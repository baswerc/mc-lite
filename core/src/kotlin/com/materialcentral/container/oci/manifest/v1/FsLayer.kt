package com.materialcentral.container.oci.manifest.v1

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import com.materialcentral.repository.container.ContainerName
import com.materialcentral.container.oci.manifest.ManifestLayer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FsLayer : ManifestLayer
{
    override val mediaType: String? = null

    override val size: Long? = null

    /**
     * The layer index.
     */
    override val index: Int

    /**
     * blobSum is the digest of the referenced filesystem image layer. A digest must be a sha256 hash.
     */
    val blobSum: String

    override val digest: String
        get() = blobSum

    constructor(index: Int, blobSum: String) {
        this.index = index
        this.blobSum = blobSum
    }

    override fun toJson(): JsonObject  = json {
        obj(
            "blobSum" to blobSum
        )
    }

    companion object {

        @Throws(IllegalArgumentException::class)
        fun parse(manifestJson: JsonObject): List<FsLayer> {
            if (!manifestJson.contains("fsLayers")) {
                throw IllegalArgumentException("Manifest does not contain a fsLayers property.")
            }


            val fsLayers = mutableListOf<FsLayer>()
            val fsLayersArray = manifestJson.array<JsonObject>("fsLayers")
            if (fsLayersArray != null) {
                // The layer list is ordered starting from top image to the base image for schema1. This is the opposite of schema2.
                for ((index, fsLayerJson) in fsLayersArray.reversed().withIndex()) {
                    val blobSum = fsLayerJson.string("blobSum")
                    if (blobSum.isNullOrBlank()) {
                        throw IllegalArgumentException("One or more fsLayers has no blobSum property.")
                    }

                    fsLayers.add(FsLayer(index, blobSum))
                }
            }

            return fsLayers
        }
    }
}