package com.materialcentral.container.oci.configuration

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import org.geezer.json.Jsonable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The rootfs key references the layer content addresses used by the image. This makes the image config hash depend on the filesystem hash.
 */
class RootFs : Jsonable {
    val json: JsonObject

    /**
     * MUST be set to layers. Implementations MUST generate an error if they encounter a unknown value while verifying or unpacking an image.
     */
    val type: String

    /**
     * An array of layer content hashes (DiffIDs), in order from first to last.
     */
    val diffIds: List<String>

    constructor(json: JsonObject, type: String, diffIds: List<String>) {
        this.json = json
        this.type = type
        this.diffIds = diffIds
    }

    override fun toJson(): JsonObject = json

    companion object {
        fun parse(imageConfigurationJson: JsonObject): Either<String, RootFs?> {
            val rootFsJson = imageConfigurationJson.obj("rootfs")

            if (rootFsJson == null) {
                return null.right()
            }

            val type = rootFsJson.string("type")
            if (type.isNullOrBlank()) {
                return "The rootfs object of the image configuration has no type property.".left()
            }

            val diffIds = rootFsJson.array<String>("diff_ids") ?: listOf()

            return RootFs(rootFsJson, type, diffIds).right()
        }
    }
}