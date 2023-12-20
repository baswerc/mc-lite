package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject

class TrivyMisconfSummary(val successes: Int, val failures: Int, val exceptions: Int) {
    companion object {
        fun map(json: JsonObject?): TrivyMisconfSummary? {
            if (json == null) {
                return null
            }

            val successes = json.int("Successes") ?: 0
            val failures = json.int("Failures") ?: 0
            val exceptions = json.int("Exceptions") ?: 0

            return TrivyMisconfSummary(successes, failures, exceptions)
        }
    }
}