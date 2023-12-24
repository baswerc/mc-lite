package com.materialcentral.container.oci.manifest.v2.list

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import com.materialcentral.container.oci.manifest.Platform
import com.materialcentral.container.oci.manifest.v2.Descriptor
import com.materialcentral.container.oci.manifest.v2.DescriptorParameters
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ManifestListManifest : Descriptor {

    val platform: Platform

    constructor(platform: Platform, parameters: DescriptorParameters) : super(parameters) {
        this.platform = platform
    }

    override fun addTo(json: JsonObject) {
        json["platform"] = platform
    }

    companion object {

        fun parse(manifestListJson: JsonObject): Either<String, List<ManifestListManifest>> {
            val manifestsJson = manifestListJson.array<JsonObject>("manifests")
            if (manifestsJson.isNullOrEmpty()) {
                return "Manifest list has no manifests property.".left()
            }

            val platforms = mutableListOf<ManifestListManifest>()
            for (manifestJson in manifestsJson) {
                val platform = when (val result = Platform.map(manifestJson)) {
                    is Either.Left -> {
                        return result
                    }
                    is Either.Right -> {
                        result.value
                    }
                }
                val parameters = when (val result = mapDescriptor(manifestJson, "manifests")) {
                    is Either.Left -> {
                        return result
                    }

                    is Either.Right -> {
                        result.value
                    }
                }
                platforms.add(ManifestListManifest(platform, parameters))
            }
            return platforms.right()
        }
    }

}