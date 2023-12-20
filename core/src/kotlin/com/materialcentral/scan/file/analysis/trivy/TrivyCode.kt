package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject

class TrivyCode(val lines: List<TrivyLine>) {
    companion object {
        fun map(json: JsonObject?): TrivyCode? {
            if (json == null) {
                return null
            }

            val lines = json.array<JsonObject>("Lines")?.mapNotNull { TrivyLine.map(it) } ?: listOf()
            return TrivyCode(lines)
        }
    }
}