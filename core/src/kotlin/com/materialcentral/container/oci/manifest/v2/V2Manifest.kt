package com.materialcentral.container.oci.manifest.v2

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import com.materialcentral.container.oci.manifest.Manifest
import com.materialcentral.container.oci.manifest.ManifestSchemaVersion
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class V2Manifest : Manifest {
    override val content: String

    override val schemaVersion: ManifestSchemaVersion = ManifestSchemaVersion.V2

    override val mediaType: String

    override val digest: String

    override val lastModifiedAt: Long?

    val config: Config

    override val layers: List<Layer>

    val annotations: List<Pair<String, String>>

    constructor(content: String, mediaType: String, digest: String, lastModifiedAt: Long?, config: Config, layers: List<Layer>, annotations: List<Pair<String, String>>) {
        this.content = content
        this.mediaType = mediaType
        this.digest = digest
        this.lastModifiedAt = lastModifiedAt
        this.config = config
        this.layers = layers
        this.annotations = annotations
    }

    override fun toString(): String = prettyPrint()

    companion object {
        val MediaTypes = listOf("application/vnd.docker.distribution.manifest.v2+json", "application/vnd.oci.image.manifest.v1+json")

        fun parse(content: String, manifestJson: JsonObject, digest: String, mediaType: String, lastModifiedAt: Long?): Either<String, V2Manifest> {
            val schemaVersion = manifestJson.int("schemaVersion")

            if (schemaVersion == null) {
                return "Manifest contains no schemaVersion property.".left()
            } else if (schemaVersion != 2) {
                return "Manifest schemaVersion property expected 1 but was $schemaVersion.".left()
            }

            val config = when (val result = Config.parse(manifestJson)) {
                is Either.Left -> {
                    return result
                }
                is Either.Right -> {
                    result.value
                }
            }

            val layers = when (val result = Layer.map(manifestJson)) {
                is Either.Left -> {
                    return result
                }

                is Either.Right -> {
                    result.value
                }
            }

            return V2Manifest(content, mediaType, digest, lastModifiedAt, config, layers, Descriptor.mapAnnotations(manifestJson.obj("annotations"))).right()
        }
    }
}