package com.materialcentral.container.oci.manifest

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import org.geezer.json.Jsonable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This OPTIONAL property describes the minimum runtime requirements of the image. This property SHOULD be present if its target is platform-specific.
 */
class Platform : Jsonable {
    /**
     * This REQUIRED property specifies the CPU architecture. Image indexes SHOULD use, and implementations SHOULD understand, values listed in the Go Language document for https://golang.org/doc/install/source#environment.
     */
    val architecture: String

    /**
     * This REQUIRED property specifies the operating system. Image indexes SHOULD use, and implementations SHOULD understand, values listed in the Go Language document for https://golang.org/doc/install/source#environment.
     */
    val os: String

    /**
     * This OPTIONAL property specifies the version of the operating system targeted by the referenced blob. Implementations MAY refuse to use manifests where os.version is not known to work with the host OS version. Valid values are
     * implementation-defined. e.g. 10.0.14393.1066 on windows.
     */
    val osVersion: String?

    /**
     * This OPTIONAL property specifies an array of strings, each specifying a mandatory OS feature. When os is windows, image indexes SHOULD use, and implementations SHOULD understand the following values:
     */
    val osFeatures: List<String>

    /**
     * This OPTIONAL property specifies the variant of the CPU. Image indexes SHOULD use, and implementations SHOULD understand, variant values listed in the Platform Variants table
     * https://github.com/opencontainers/image-spec/blob/main/image-index.md#platform-variants.
     */
    val variant: String?

    /**
     * The optional features field specifies an array of strings, each listing a required CPU feature (for example sse4 or aes).
     */
    val features: List<String>

    constructor(architecture: String, os: String, osVersion: String?, osFeatures: List<String>, variant: String?, features: List<String>) {
        this.architecture = architecture
        this.os = os
        this.osVersion = osVersion
        this.osFeatures = osFeatures
        this.variant = variant
        this.features = features
    }

    override fun toJson(): JsonObject = json {
        obj(
            "architecture" to architecture,
            "os" to os,
            "os.version" to osVersion,
            "os.architecture" to architecture,
            "variant" to  variant
        )
    }

    companion object {
        fun map(json: JsonObject): Either<String, Platform> {

            val platformJson = json.obj("platform")
            if (platformJson == null) {
                return "Platform manifest has no platform object.".left()
            }

            val architecture = platformJson.string("architecture")
            if (architecture.isNullOrBlank()) {
                return "Platform contains no architecture property.".left()
            }

            val os = platformJson.string("os")
            if (os.isNullOrBlank()) {
                return "Platform contains no os property.".left()
            }

            val osVersion = platformJson.string("os.version")
            val osFeatures = platformJson.array<String>("os.features") ?: listOf()
            val variant = platformJson.string("variant")
            val features = platformJson.array<String>("features") ?: listOf()

            return Platform(architecture, os, osVersion, osFeatures, variant, features).right()

        }
    }
}