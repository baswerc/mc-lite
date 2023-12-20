package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/fanal/types/artifact.go#L13
 */
class TrivyOS(val family: TrivyOsFamily?, val name: String, val eosl: Boolean?) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(javaClass)

        fun map(json: JsonObject?): TrivyOS? {
            if (json == null) {
                return null
            }

            val family = TrivyOsFamily.map(json.string("Family"))
            if (family == null) {
                log.warn("Trivy OS has no known Family property in JSON: ${json.toJsonString()}")
                return null
            }

            val name = json.string("Name")
            if (name.isNullOrBlank()) {
                log.warn("Trivy OS has no Name property in JSON: ${json.toJsonString()}")
                return null
            }

            return TrivyOS(family, name, json.boolean("Eosl"))
        }
    }
}