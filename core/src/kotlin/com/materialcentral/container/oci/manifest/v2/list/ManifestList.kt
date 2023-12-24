package com.materialcentral.container.oci.manifest.v2.list

import arrow.core.Either
import com.beust.klaxon.JsonObject
import com.materialcentral.container.oci.manifest.Manifest
import com.materialcentral.container.oci.manifest.ManifestLayer
import com.materialcentral.container.oci.manifest.ManifestSchemaVersion

/**
 * The manifest list is the “fat manifest” which points to specific image manifests for one or more platforms. Its use is optional, and relatively few images will use one of these manifests. A client will distinguish a manifest list
 * from an image manifest based on the Content-Type returned in the HTTP response.
 */
class ManifestList : Manifest {
    override val content: String

    override val schemaVersion: ManifestSchemaVersion = ManifestSchemaVersion.V2

    /**
     * The MIME type of the manifest.
     */
    override val mediaType: String

    override val digest: String

    override val lastModifiedAt: Long?

    override val layers: List<ManifestLayer> = listOf()

    /**
     * This REQUIRED property contains a list of manifests for specific platforms.
     */
    val manifests: List<ManifestListManifest>

    constructor(content: String, mediaType: String, digest: String, lastModifiedAt: Long?, manifests: List<ManifestListManifest>) {
        this.content = content
        this.mediaType = mediaType
        this.digest = digest
        this.lastModifiedAt = lastModifiedAt
        this.manifests = manifests
    }

    override fun toString(): String = prettyPrint()

    companion object {
        val MediaTypes = listOf("application/vnd.docker.distribution.manifest.list.v2+json", "application/vnd.oci.image.index.v1+json")

        fun parse(content: String, manifestJson: JsonObject, digest: String, mediaType: String, lastModifiedAt: Long?): Either<String, ManifestList> {
            val manifests = ManifestListManifest.parse(manifestJson)
            return manifests.map { ManifestList(content, mediaType, digest, lastModifiedAt, it) }
        }
    }
}