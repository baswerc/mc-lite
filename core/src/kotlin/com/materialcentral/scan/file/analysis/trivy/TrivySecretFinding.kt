package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject
import com.materialcentral.scan.FindingSeverity
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/fanal/types/secret.go#L10
 */
class TrivySecretFinding(
    val ruleID: String,
    val category: String,
    val severity: FindingSeverity,
    val title: String?,
    val startLine: Int?,
    val endLine: Int?,
    val match: String?,
    val layer: TrivyLayer?
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(javaClass)

        fun map(json: JsonObject?): TrivySecretFinding? {
            if (json == null) {
                return null
            }

            val ruleID = json.string("RuleID")
            if (ruleID.isNullOrBlank()) {
                log.warn("Trivy SecretFinding has no RuleID property in JSON: ${json.toJsonString()}")
                return null
            }
            val category = json.string("Category")
            if (category.isNullOrBlank()) {
                log.warn("Trivy SecretFinding has no Category property in JSON: ${json.toJsonString()}")
                return null
            }

            val severity = trivySeverity(json.string("Severity"))
            if (severity == null) {
                log.warn("Trivy SecretFinding invalid Severity property in JSON: ${json.toJsonString()}")
                return null
            }

            val title = json.string("Title")
            val startLine = json.int("StartLine")
            val endLine = json.int("EndLine")
            val match = json.string("Match")
            val layer = TrivyLayer.map(json.obj("Layer"))

            return TrivySecretFinding(ruleID, category, severity, title, startLine, endLine, match, layer)
        }
    }
}