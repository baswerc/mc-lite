package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject
import org.geezer.toJsonObjectOrBust
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/types/report.go#L12
 */
class TrivyReport(
    val schemaVersion: Int,
    val artifactName: String,
    val artifactType: TrivyArtifactType?,
    val metadata: TrivyMetadata?,
    val results: List<TrivyResult>
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(javaClass)

        fun parse(jsonString: String?): TrivyReport? {
            if (jsonString.isNullOrBlank()) {
                return null
            }

            val json = try {
                jsonString.toJsonObjectOrBust()
            } catch (e: Exception) {
                log.error("Unable to parse trivy output JSON: ${ExceptionUtils.getRootCauseMessage(e)}")
                null
            }

            return if (json == null) null else map(json)
        }

        fun map(json: JsonObject): TrivyReport? {
            val schemaVersion = json.int("SchemaVersion")
            if (schemaVersion == null) {
                log.error("Trivy output contains no schema version.")
                return null
            }

            if (schemaVersion != 2) {
                log.warn("Unexpected schema version $schemaVersion. Will try to parse trivy output anyway.")
            }

            val artifactType = TrivyArtifactType.fromId(json.string("ArtifactType"))
            val metadata = TrivyMetadata.map(json.obj("Metadata"))
            val results = json.array<JsonObject>("Results")?.mapNotNull { TrivyResult.parse(it) } ?: listOf()
            return TrivyReport(schemaVersion, json.string("ArtifactName") ?: "", artifactType, metadata, results)
        }
    }
}
