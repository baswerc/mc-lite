package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject
import com.materialcentral.oss.PackageType
import org.geezer.toJsonArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/types/report.go#L101
 */
class TrivyResult(
    val target: String,
    val resultClass: TrivyResultClass,
    val type: TrivyResultType,
    val packages: List<TrivyPackage>,
    val vulnerabilities: List<TrivyDetectedVulnerability>,
    val misconfSummary: TrivyMisconfSummary?,
    val misconfigurations: List<TrivyDetectedMisconfiguration>,
    val secrets: List<TrivySecretFinding>,
    val licenses: List<TrivyDetectedLicense>,
    val customResources: List<TrivyCustomResource>,
) {

    val packageType: PackageType?
        get() = type.packageType

    companion object {
        private val log: Logger = LoggerFactory.getLogger(javaClass)

        fun parse(jsonString: String): List<TrivyResult> = jsonString.toJsonArray<JsonObject>()?.mapNotNull { parse(it) } ?: listOf()

        fun parse(json: JsonObject?): TrivyResult? {
            if (json == null) {
                return null
            }

            val target = json.string("Target")
            if (target.isNullOrBlank()) {
                log.warn("Trivy result has no Target property in JSON: ${json.toJsonString()}")
                return null
            }

            val resultClass = TrivyResultClass.fromId(json.string("Class"))
            if (resultClass == null) {
                log.warn("Trivy result has known Class property in JSON: ${json.toJsonString()}")
                return null
            }

            var type = TrivyResultType.fromId(json.string("Type"))
            if (type == null && resultClass == TrivyResultClass.OS_PACKAGES) {
                log.warn("Unknown OS Trivy result type: ${json.string("Type")}")
                type = TrivyResultType.OS_RELEASE
            }

            if (type == null) {
                log.warn("Trivy result has known Class property in JSON: ${json.toJsonString()}")
                return null
            }

            val packages = json.array<JsonObject>("Packages")?.mapNotNull { TrivyPackage.map(it) } ?: listOf()
            val vulnerabilities = json.array<JsonObject>("Vulnerabilities")?.mapNotNull { TrivyDetectedVulnerability.map(it) } ?: listOf()
            val misconfSummary = TrivyMisconfSummary.map(json.obj("MisconfSummary"))
            val misconfigurations = json.array<JsonObject>("Misconfigurations")?.mapNotNull { TrivyDetectedMisconfiguration.map(it) } ?: listOf()
            val secrets = json.array<JsonObject>("Secrets")?.mapNotNull { TrivySecretFinding.map(it) } ?: listOf()
            val licenses = json.array<JsonObject>("Licenses")?.mapNotNull { TrivyDetectedLicense.map(it) } ?: listOf()
            val customResources = json.array<JsonObject>("CustomResources")?.mapNotNull { TrivyCustomResource.map(it) } ?: listOf()

            val result = TrivyResult(target, resultClass, type, packages, vulnerabilities, misconfSummary, misconfigurations, secrets, licenses, customResources)
            packages.forEach { it.result = result }
            vulnerabilities.forEach { it.result = result }
            return result
        }
    }
}
