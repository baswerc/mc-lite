package com.materialcentral.container.oci.manifest.v2

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import com.materialcentral.container.oci.manifest.ManifestLayer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://github.com/opencontainers/image-spec/blob/main/manifest.md
 */
class Layer : Descriptor, ManifestLayer {

    override val index: Int

    constructor(index: Int, parameters: DescriptorParameters) : super(parameters) {
        this.index = index
    }

    override fun addTo(json: JsonObject) {}

    companion object {

        fun map(manifestJson: JsonObject): Either<String, List<Layer>> {
            val layersJson = manifestJson.array<JsonObject>("layers")
            if (layersJson.isNullOrEmpty()) {
                return "Manifest contains no layers.".left()
            }

            val layers = mutableListOf<Layer>()
            for ((index, layerJson) in layersJson.withIndex()) {
                when (val result = mapDescriptor(layerJson, "layer")) {
                    is Either.Left -> {
                        return result.value.left()
                    }
                    is Either.Right -> {
                        layers.add(Layer(index, result.value))
                    }
                }
            }

            return layers.right()
        }
    }

}