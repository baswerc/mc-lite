package com.materialcentral.container.image.scan.analysis

import com.materialcentral.os.Architecture
import com.materialcentral.os.LinuxDistribution
import com.materialcentral.os.OperatingSystemType
import com.materialcentral.container.image.scan.ContainerImageResourceType
import com.materialcentral.scan.Scan
import com.materialcentral.scan.analysis.AnalysisConfiguration
import com.materialcentral.scan.file.analysis.trivy.TrivyAnalyzer
import com.materialcentral.scan.file.analysis.trivy.TrivyResultClass
import java.io.File

object TrivyContainerImageAnalyzer : TrivyAnalyzer(), ContainerImageAnalyzer {

    override val canAnalyzeOCIDirectory: Boolean = true

    override val canAnalyzeRootFSDirectory: Boolean = true

    override val canAnalyzeCycleDX: Boolean = true

    override val canAnalyzeSpdx: Boolean = true

    override fun analyze(scan: Scan, analysisConfiguration: AnalysisConfiguration, imageResource: File, imageResourceType: ContainerImageResourceType): ContainerImageAnalysis {
        val subcommands = mutableListOf<String>()
        when (imageResourceType) {
            ContainerImageResourceType.OCI_DIR -> {
                subcommands.add("image")

            }

            ContainerImageResourceType.ROOT_FS_DIR -> {
                subcommands.add("rootfs")
            }

            ContainerImageResourceType.CYCLONE_DX_FILE,
            ContainerImageResourceType.SPDX_FILE -> {
                subcommands.add("sbom")
            }
        }

        subcommands.add("--input")
        subcommands.add(imageResource.absolutePath)

        val (report, findings) = runTrivyCmd(scan, analysisConfiguration, subcommands)

        var linuxDistribution = report.metadata?.os?.family?.linuxDistribution
        var os = if (linuxDistribution != null) OperatingSystemType.LINUX else report.metadata?.os?.family?.os

        if (os == null) {
            for (result in report.results.filter { it.resultClass == TrivyResultClass.OS_PACKAGES }) {
                val packageType = result.packageType
                if (packageType != null) {
                    linuxDistribution = LinuxDistribution.fromPackageType(packageType)
                    if (linuxDistribution != null) {
                        os = OperatingSystemType.LINUX
                        break
                    }
                }
            }
        }

        val architecture = Architecture.mapOptionalReadableId(report.metadata?.imageConfiguration?.architecture)

        return ContainerImageAnalysis(os, linuxDistribution, architecture, analysisConfiguration, findings)
    }
}