package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/fanal/types/artifact.go#L59
 */
class TrivyLayer(
    val diffId: String?,
    val digest: String?,
    val createdBy: String?
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(javaClass)

        fun map(json: JsonObject?): TrivyLayer? {
            if (json == null) {
                return null
            }

            val diffId = json.string("DiffID")
            if (diffId.isNullOrBlank()) {
                log.warn("Trivy layer ${json.toJsonString()} has no DiffID property.")
                return null
            }

            val digest = json.string("Digest")
            if (digest.isNullOrBlank()) {
                log.warn("Trivy layer ${json.toJsonString()} has no Digest property.")
                return null
            }

            val createdBy = json.string("CreatedBy")

            return TrivyLayer(diffId, digest, createdBy)
        }
    }
}
