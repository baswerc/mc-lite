package com.materialcentral.container.oci.manifest.v1

import com.beust.klaxon.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * History is a list of unstructured historical data for v1 compatibility. It contains ID of the image layer and ID of the layer’s parent layers.
 */
class History {

    /**
     * V1Compatibility is the raw V1 compatibility information. This will contain the JSON object describing the V1 of this image.
     */
    val v1Compatibility: String

    constructor(v1Compatibility: String) {
        this.v1Compatibility = v1Compatibility
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(javaClass)

        fun parse(manifestJson: JsonObject): List<History> {
            val history = mutableListOf<History>()
            val historyJsons = manifestJson.array<JsonObject>("history")
            if (historyJsons != null) {
                for (historyJson in historyJsons) {
                    val v1Compatibility = historyJson.string("v1Compatibility")
                    if (v1Compatibility.isNullOrBlank()) {
                        log.warn("Manifest history object has no v1Compatibility property.")
                    } else {
                        history.add(History(v1Compatibility))
                    }
                }
            }
            return history
        }
    }
}