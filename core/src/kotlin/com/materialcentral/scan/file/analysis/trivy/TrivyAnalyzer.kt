package com.materialcentral.scan.file.analysis.trivy

import arrow.core.nonEmptyListOf
import com.materialcentral.scan.Scan
import com.materialcentral.scan.FindingType
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.analysis.AnalysisConfiguration
import com.materialcentral.scan.analysis.AnalyzerBase
import com.materialcentral.scan.file.analysis.*
import org.geezer.system.runtime.StringProperty
import org.geezer.io.IO
import org.geezer.causeMessage
import java.io.File
import java.io.FileWriter

abstract class TrivyAnalyzer : AnalyzerBase() {
    override val toolId: String = "trivy"

    override val toolName: String = "Trivy"

    override val url: String = "https://github.com/aquasecurity/trivy"

    override val description: String = "Trivy is the most popular open source security scanner, reliable, fast, and easy to use. Use Trivy to find vulnerabilities & IaC misconfigurations, SBOM discovery, Cloud scanning, Kubernetes security risks,and more.\n"

    override val mediums = nonEmptyListOf(ScanMedium.METADATA, ScanMedium.ASSET)

    override val findingTypes = nonEmptyListOf(FindingType.OSS_PACKAGE_RELEASE, FindingType.KNOWN_VULNERABILITY, FindingType.SECRET, FindingType.MISCONFIGURATION)

    override val hasToolSpecificConfiguration: Boolean = true

    protected fun runTrivyCmd(scan: Scan, analysisConfiguration: AnalysisConfiguration, subcommands: List<String>): Pair<TrivyReport, List<FileAnalysisFinding>> {
        return IO.withTempDir("trivy") { trivyOutputDir ->
            val findings = mutableListOf<FileAnalysisFinding>()

            val outputFile = File(trivyOutputDir, OutputFileName)
            val cmds = mutableListOf(trivyCmd())
            cmds.add("--output")
            cmds.add(outputFile.absolutePath)
            cmds.add("--format")
            cmds.add("json")

            cmds.addAll(subcommands)

            if (!cmds.contains("--scanners")) {
                val scanners = mutableListOf<String>()
                for (findingType in analysisConfiguration.findingTypes) {
                    when (findingType) {
                        FindingType.OSS_PACKAGE_RELEASE -> {
                            if (!cmds.contains("--list-all-pkgs")) {
                                cmds.add("--list-all-pkgs")
                                scanners.add("license")
                            }
                        }

                        FindingType.KNOWN_VULNERABILITY -> {
                            scanners.add("vuln")
                        }

                        FindingType.SECRET -> {
                            scanners.add("secret")
                        }

                        FindingType.MISCONFIGURATION -> {
                            scanners.add("config")
                        }
                    }
                }

                if (scanners.isNotEmpty()) {
                    cmds.add("--scanners")
                    cmds.add(scanners.joinToString(","))
                }
            }

            runAnalyzerCommand(cmds, scan)

            scan.checkpoint()

            if (!outputFile.isFile) {
                log.error("Successfully executed trivy command but no output file found at: ${outputFile.absolutePath}.")
                throw scan.failed("Trivy process failed to produce output file.")
            }

            val reportJson = try {
                outputFile.readBytes().decodeToString()
            } catch (e: Exception) {
                log.error("Successfully executed trivy command but output file at: ${outputFile.absolutePath} can not be read due to: ${e.causeMessage}", e)
                throw scan.failed("Trivy process failed to produce readable output file.")
            }

            FileWriter("/tmp/trivy.json").use { writer -> writer.write(reportJson) }

            val report = TrivyReport.parse(reportJson)

            if (report == null) {
                log.error("Unable to parse trivy output file: $reportJson")
                throw scan.failed("Unable to parse trivy output file.")
            }

            val windowsOs = report.metadata?.os?.family == TrivyOsFamily.WINDOWS

            for (result in report.results) {
                when (result.resultClass) {
                    TrivyResultClass.OS_PACKAGES,
                    TrivyResultClass.LANGUAGE_PACKAGES -> {

                        val packageType = result.packageType
                        val filePathsByPackages = mutableMapOf<String, String>()
                        if (packageType != null) {
                            for (pkg in result.packages) {
                                val packageName = pkg.name
                                var packageVersion = pkg.version
                                if (packageName.isNotBlank() && !packageVersion.isNullOrBlank()) {

                                    if (!pkg.release.isNullOrBlank()) {
                                        packageVersion = "$packageVersion-${pkg.release}"
                                    }

                                    var filePath = pkg.filePath
                                    if (filePath.isNullOrBlank() && result.resultClass == TrivyResultClass.LANGUAGE_PACKAGES) {
                                        filePath = result.target
                                    }
                                    if (!windowsOs && filePath != null && !filePath.startsWith("/")) {
                                        filePath = "/$filePath"
                                    }

                                    val layerId = pkg.layer?.digest
                                    // TODO Does trivy capture size or digests of package files?
                                    findings.add(OssPackageReleaseAnalysisFinding(packageType, packageName, packageVersion, null, null, null, null, filePath, layerId, id))
                                }
                            }
                        }

                        for (vulnerability in result.vulnerabilities) {
                            val packageType = result.packageType
                            val packageName = vulnerability.packageName
                            val packageVersion = vulnerability.installedVersion
                            var filePath = vulnerability.packagePath

                            if (packageType != null && !packageName.isNullOrBlank() && !packageVersion.isNullOrBlank()) {
                                if (result.resultClass == TrivyResultClass.LANGUAGE_PACKAGES && filePath.isNullOrBlank()) {
                                    filePath = result.packages.firstOrNull { it.name == packageName && it.version == packageVersion && it.filePath != null }?.filePath
                                }

                                if (filePath.isNullOrBlank() && result.resultClass != TrivyResultClass.OS_PACKAGES) {
                                        filePath = result.target
                                }

                                if (filePath != null && !windowsOs && !filePath.startsWith("/")) {
                                    filePath = "/$filePath"
                                }

                                val layerDiffId = vulnerability.layer?.diffId
                                findings.add(KnownVulnerabilityAnalysisFinding(nonEmptyListOf(vulnerability.id), packageType, packageName, packageVersion, filePath, layerDiffId, id, vulnerability.severity))
                            } else {
                                log.warn("Trivy vulnerability ${vulnerability.id} of result class ${result.resultClass.id} has no package coordinates.")
                            }

                        }
                    }

                    TrivyResultClass.CONFIG -> {
                        var filePath = result.target
                        if (!windowsOs && !filePath.startsWith("/")) {
                            filePath = "/$filePath"
                        }

                        for (misconfiguration in result.misconfigurations) {
                            val startLine = misconfiguration.causeMetadata?.startLine
                            val endLine = misconfiguration.causeMetadata?.endLine
                            val layerId = misconfiguration.layer?.digest
                            findings.add(MisconfigurationAnalysisFinding(misconfiguration.id, misconfiguration.title, misconfiguration.description,
                                misconfiguration.message ?: misconfiguration.resolution, startLine, endLine, misconfiguration.referenceUrls, filePath, layerId, id, misconfiguration.severity))
                        }
                    }

                    TrivyResultClass.SECRET -> {
                        var filePath = result.target
                        if (!windowsOs && !filePath.startsWith("/")) {
                            filePath = "/$filePath"
                        }

                        for (secret in result.secrets) {
                            val startLine = secret.startLine
                            val endLine = secret.endLine
                            val layerId = secret.layer?.digest
                            findings.add(SecretAnalysisFinding(secret.ruleID, secret.title, null, startLine, endLine, listOf(), filePath, layerId, id, secret.severity))
                        }
                    }

                    else -> {}
                }
            }

            report to findings
        }
    }

    companion object {
        val trivyCmd = StringProperty("TrivyCmd", "trivy")

        const val OutputFileName = "trivyOutput.json"
    }
}