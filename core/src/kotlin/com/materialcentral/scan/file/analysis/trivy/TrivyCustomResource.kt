package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject

class TrivyCustomResource(
    val type: String?,
    val filePath: String?,
    val data: JsonObject?
) {
    companion object {
        fun map(json: JsonObject): TrivyCustomResource? {
            val type = json.string("type")
            val filePath = json.string("filePath")
            val data = json.obj("data")
            return TrivyCustomResource(type, filePath, data)
        }
    }
}