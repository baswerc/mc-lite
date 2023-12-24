package com.materialcentral.container.oci.configuration

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import org.geezer.causeMessage
import org.geezer.json.Jsonable
import org.geezer.digest.Digester
import org.geezer.toJsonObjectOrBust
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class ImageConfiguration : Jsonable {
    val content: String

    val digest: String

    /**
     * An combined date and time at which the image was created, formatted as defined by RFC 3339, section 5.6 - https://tools.ietf.org/html/rfc3339#section-5.6.
     */
    val created: String?

    /**
     * Gives the name and/or email address of the person or entity which created and is responsible for maintaining the image.
     */
    val author: String?

    /**
     * The CPU architecture which the binaries in this image are built to run on. Configurations SHOULD use, and implementations SHOULD understand, values listed in the Go Language document for
     * GOARCH - https://golang.org/doc/install/source#environment.
     */
    val architecture: String?

    /**
     * The name of the operating system which the image is built to run on. Configurations SHOULD use, and implementations SHOULD understand, values listed in the Go Language document for GOOS - https://golang.org/doc/install/source#environment.
     */
    val os: String?

    /**
     * This OPTIONAL property specifies the version of the operating system targeted by the referenced blob. Implementations MAY refuse to use manifests where os.version is not known to work with the host OS version. Valid values
     * are implementation-defined. e.g. 10.0.14393.1066 on windows.
     */
    val osVersion: String?

    /**
     * This OPTIONAL property specifies an array of strings, each specifying a mandatory OS feature. When os is windows, image indexes SHOULD use, and implementations SHOULD understand the following values:
     */
    val osFeatures: List<String>

    /**
     * The variant of the specified CPU architecture. Configurations SHOULD use, and implementations SHOULD understand, variant values listed in the Platform Variants table -
     * https://github.com/opencontainers/image-spec/blob/main/image-index.md#platform-variants.
     */
    val variant: String?

    /**
     * The execution parameters which SHOULD be used as a base when running a container using the image. This field can be null, in which case any execution parameters should be specified at creation of the container.
     */
    val config: Config?

    /**
     * The rootfs key references the layer content addresses used by the image. This makes the image config hash depend on the filesystem hash.
     */
    val rootFs: RootFs?

    /**
     * Describes the history of each layer. The array is ordered from first to last. The object has the following fields:
     */
    val history: List<History>

    constructor(content: String, digest: String, created: String?, author: String?, architecture: String?, os: String?, osVersion: String?, osFeatures: List<String>, variant: String?, config: Config?, rootFs: RootFs?, history: List<History>) {
        this.content = content
        this.digest = digest
        this.created = created
        this.author = author
        this.architecture = architecture
        this.os = os
        this.osVersion = osVersion
        this.osFeatures = osFeatures
        this.variant = variant
        this.config = config
        this.rootFs = rootFs
        this.history = history
    }

    override fun toJson(): JsonObject = content.toJsonObjectOrBust()

    companion object {
        val MediaTypes = mutableListOf("application/vnd.docker.container.image.v1+json", "application/vnd.oci.image.config.v1+json")

        fun parse(jsonString: String?, digest: String?): Either<String, ImageConfiguration> {
            if (jsonString.isNullOrBlank()) {
                return "No content returned for the image configuration.".left()
            }

            val json = try {
                jsonString.toJsonObjectOrBust()
            } catch (e: Exception) {
                return "Unable to parse image configuration JSON: $jsonString due to: ${e.causeMessage}".left()
            }

            return map(json, digest, jsonString).right()
        }

        fun map(json: JsonObject, digest: String? = null, jsonString: String? = null): ImageConfiguration {
            val jsonString = jsonString ?: json.toJsonString()
            val digest = digest ?: "sha256:${Digester.sha256(jsonString)}"

            val created = json.string("created")
            val author = json.string("author")
            val architecture = json.string("architecture")
            val os = json.string("os")
            val osVersion = json.string("os.version")
            val osFeatures = json.array<String>("os.features") ?: listOf()
            val variant = json.string("variant")
            val config = Config.parse(json)
            val rootFs = RootFs.parse(json)
            val history = History.parse(json)

            return ImageConfiguration(jsonString, digest, created,  author, architecture, os, osVersion, osFeatures, variant, config, rootFs, history)
        }
    }
}
