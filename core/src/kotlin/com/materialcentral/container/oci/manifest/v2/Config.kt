package com.materialcentral.container.oci.manifest.v2

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://github.com/opencontainers/image-spec/blob/main/manifest.md
 */
class Config : Descriptor {

    constructor(parameters: DescriptorParameters) : super(parameters)

    override fun addTo(json: JsonObject) {}

    companion object {
        fun parse(manifestJson: JsonObject): Either<String, Config> {
            val configJson = manifestJson.obj("config")
            if (configJson == null) {
                return "Manifest contains no config object.".left()
            }

            val parameters = mapDescriptor(configJson, "config")
            return parameters.map { Config(it) }
        }
    }
}