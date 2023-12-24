package com.materialcentral.container.oci.manifest.v1

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import com.materialcentral.container.oci.manifest.Manifest
import com.materialcentral.container.oci.manifest.ManifestLayer
import com.materialcentral.container.oci.manifest.ManifestSchemaVersion

/**
 * https://docs.docker.com/registry/spec/manifest-v2-1/
 *
 * Manifest provides the base accessible fields for working with V2 image format in the registry.
 *
 */
class V1Manifest : Manifest {

    override val content: String

    override val mediaType: String

    override val schemaVersion: ManifestSchemaVersion = ManifestSchemaVersion.V1

    override val digest: String

    override val lastModifiedAt: Long?

    /**
     * The name of the image’s repository.
     */
    val name: String

    /**
     * the tag of the image.
     */
    val tag: String?

    /**
     * Architecture is the host architecture on which this image is intended to run.
     */
    val architecture: String?

    val fsLayers: List<FsLayer>

    override val layers: List<ManifestLayer>
        get() = fsLayers

    val history: List<History>

    val signatures: List<Signature>

    constructor(content: String, contentType: String, digest: String, lastModifiedAt: Long?, name: String, tag: String?, architecture: String?, fsLayers: List<FsLayer>, history: List<History>, signatures: List<Signature>) {
        this.content = content
        this.mediaType = contentType
        this.digest = digest
        this.lastModifiedAt = lastModifiedAt
        this.name = name
        this.tag = tag
        this.architecture = architecture
        this.fsLayers = fsLayers
        this.history = history
        this.signatures = signatures
    }

    override fun toString(): String = prettyPrint()

    companion object {
        val MediaTypes = listOf("application/vnd.docker.distribution.manifest.v1+prettyjws", "application/vnd.docker.distribution.manifest.v1+json")

        fun parse(content: String, manifestJson: JsonObject, contentType: String, digest: String, lastModifiedAt: Long?): Either<String, V1Manifest> {
            val schemaVersion = manifestJson.int("schemaVersion")

            if (schemaVersion == null) {
                return "Manifest contains no schemaVersion property.".left()
            } else if (schemaVersion != 1) {
                return "Manifest schemaVersion property expected 1 but was $schemaVersion.".left()
            }

            val name = manifestJson.string("name")
            if (name.isNullOrBlank()) {
                throw IllegalArgumentException("Manifest contains no name property.")
            }

            val tag = manifestJson.string("tag")
            val architecture = manifestJson.string("architecture")

            val fsLayers = FsLayer.parse(manifestJson)
            val history = History.parse(manifestJson)
            val signatures = Signature.parseSignatures(manifestJson,)
            return V1Manifest(content, contentType, digest, lastModifiedAt, name, tag, architecture, fsLayers, history, signatures).right()
        }
    }
}