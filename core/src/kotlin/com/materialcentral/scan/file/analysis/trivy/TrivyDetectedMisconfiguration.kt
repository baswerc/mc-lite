package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject
import com.materialcentral.scan.FindingSeverity
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/types/misconfiguration.go
 */
class TrivyDetectedMisconfiguration(
    val type: String,
    val id: String,
    val avdid: String?,
    val severity: FindingSeverity,
    val status: TrivyMisconfStatus,
    val title: String?,
    val description: String?,
    val message: String?,
    val namespace: String?,
    val query: String?,
    val resolution: String?,
    val primaryUrl: String?,
    val referenceUrls: List<String>,
    val layer: TrivyLayer?,
    val causeMetadata: TrivyCauseMetadata?) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(javaClass)

        fun map(json: JsonObject?): TrivyDetectedMisconfiguration? {
            if (json == null) {
                return null
            }

            val type = json.string("Type")
            if (type.isNullOrEmpty()) {
                log.warn("Trivy misconfiguration has no Type property: ${json.toJsonString()}")
                return null
            }

            val id = json.string("ID")
            if (id.isNullOrEmpty()) {
                log.warn("Trivy misconfiguration has no ID property: ${json.toJsonString()}")
                return null
            }

            val avdId = json.string("AVDID")

            val severity = trivySeverity(json.string("Severity"))
            if (severity == null) {
                log.warn("Trivy misconfiguration has no Severity property in: ${json.toJsonString()}")
                return null
            }

            val status = TrivyMisconfStatus.fromId(json.string("Status"))
            if (status == null) {
                log.warn("Trivy misconfiguration has invalid Status property: ${json.toJsonString()}")
                return null
            }



            val title = json.string("Title")
            val description = json.string("Description")
            val message = json.string("Message")
            val namespace = json.string("Namespace")
            val query = json.string("Query")
            val resolution = json.string("Resolution")
            val primaryUrl = json.string("PrimaryURL")
            val references = json.array<String>("References") ?: listOf()
            val layer = TrivyLayer.map(json.obj("Layer"))
            val causeMetadata = TrivyCauseMetadata.map(json.obj("CauseMetadata"))

            return TrivyDetectedMisconfiguration(type, id, avdId, severity, status, title, description, message, namespace, query, resolution, primaryUrl, references, layer, causeMetadata)
        }
    }

}