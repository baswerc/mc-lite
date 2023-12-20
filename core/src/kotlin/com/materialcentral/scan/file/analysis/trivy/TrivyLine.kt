package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject

class TrivyLine(val number: Int?, val content: String?, val isCause: Boolean?, val annotation: String?, val truncated: Boolean?, val highlighted: String?, val firstCause: Boolean?, val lastCause: Boolean?) {
    companion object {
        fun map(json: JsonObject?): TrivyLine? {
            if (json == null) {
                return null
            }

            val number = json.int("Number")
            val content = json.string("Content")
            val isCause = json.boolean("IsCause")
            val annotation = json.string("Annotation")
            val truncated = json.boolean("Truncated")
            val highlighted = json.string("Highlighted")
            val firstCause = json.boolean("FirstCause")
            val lastCause = json.boolean("LastCause")

            return TrivyLine(number, content, isCause, annotation, truncated, highlighted, firstCause, lastCause)
        }
    }
}