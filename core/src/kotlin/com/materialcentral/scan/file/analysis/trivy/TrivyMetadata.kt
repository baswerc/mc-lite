package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject
import com.materialcentral.repository.container.registry.oci.configuration.ImageConfiguration

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/types/report.go#L24
 */
class TrivyMetadata(
    val size: Long?,
    val os: TrivyOS?,
    val imageID: String?,
    val diffIDs: List<String>,
    val repoTags: List<String>,
    val repoDigests: List<String>, 
    val imageConfiguration: ImageConfiguration?
) {
    companion object {
        fun map(json: JsonObject?): TrivyMetadata? {
            if (json == null) {
                return null
            }

            val size = json.long("size")
            val os = TrivyOS.map(json.obj("OS"))
            val imageID = json.string("ImageID")
            val diffIDs = json.array<String>("DiffIDs") ?: listOf()
            val repoTags = json.array<String>("RepoTags") ?: listOf()
            val repoDigests = json.array<String>("RepoDigests") ?: listOf()
            val imageConfiguration = ImageConfiguration.map(json.obj("ImageConfig"))

            return TrivyMetadata(size, os, imageID, diffIDs, repoTags, repoDigests, imageConfiguration)
        }
    }
}