package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject

/**
 * https://github.com/aquasecurity/trivy/blob/85cca559305936827ce557130d88918e6d6891a1/pkg/fanal/types/misconf.go#L31
 */
class TrivyCauseMetadata(val resource: String?, val provider: String?, val service: String?, val startLine: Int?, val endLine: Int?, val code: TrivyCode?) {
    companion object {
        fun map(json: JsonObject?): TrivyCauseMetadata? {
            if (json == null) {
                return null
            }

            val resource = json.string("Resource")
            val provider = json.string("Provider")
            val service = json.string("Service")
            val startLine = json.int("StartLine")
            val endLine = json.int("EndLine")
            val code = TrivyCode.map(json.obj("Code"))

            return TrivyCauseMetadata(resource, provider, service, startLine, endLine, code)
        }
    }
}