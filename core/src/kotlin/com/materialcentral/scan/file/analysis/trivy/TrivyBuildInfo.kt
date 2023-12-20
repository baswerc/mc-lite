package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject

/**
 * BuildInfo represents information under /root/buildinfo in RHEL
 * https://github.com/aquasecurity/trivy/blob/main/pkg/fanal/types/artifact.go#L52
 */
class TrivyBuildInfo(val contentSets: List<String>, val nvr: String?, val arch: String?) {
    companion object {
        fun map(json: JsonObject?): TrivyBuildInfo? {
            if (json == null) {
                return null
            }

            val contentSets = json.array<String>("ContentSets") ?: listOf()
            val nvr = json.string("Nvr")
            val arch = json.string("Arch")

            return TrivyBuildInfo(contentSets, nvr, arch)
        }
    }
}