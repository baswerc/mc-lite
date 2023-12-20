package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TrivyDataSource(val id: String, val name: String?, val url: String?) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(javaClass)

        fun parse(json: JsonObject?): TrivyDataSource? {
            if (json == null) {
                return null
            }

            val id = json.string("ID")
            if (id.isNullOrBlank()) {
                log.warn("No ID property on Trivy datasource ${json.toJsonString()}")
                return null
            }

            return TrivyDataSource(id, json.string("Name"), json.string("URL"))
        }
    }
}