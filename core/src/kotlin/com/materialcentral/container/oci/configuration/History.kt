package com.materialcentral.container.oci.configuration

import com.beust.klaxon.JsonObject
import org.geezer.json.Jsonable

/**
 * Describes the history of each layer. The array is ordered from first to last.
 */
class History : Jsonable {
    val json: JsonObject

    /**
     * A combined date and time at which the layer was created, formatted as defined by RFC 3339, section 5.6 - https://tools.ietf.org/html/rfc3339#section-5.6.
     */
    val created: String?

    /**
     * The author of the build point.
     */
    val author: String?

    /**
     * The command which created the layer.
     */
    val createdBy: String?

    /**
     * A custom message set when creating the layer.
     */
    val comment: String?

    /**
     * This field is used to mark if the history item created a filesystem diff. It is set to true if this history item doesn't correspond to an actual layer in the rootfs section (for example, Dockerfile's ENV command results in no
     * change to the filesystem).
     */
    val emptyLayer: Boolean?

    constructor(json: JsonObject, created: String?, author: String?, createdBy: String?, comment: String?, emptyLayer: Boolean?) {
        this.json = json
        this.created = created
        this.author = author
        this.createdBy = createdBy
        this.comment = comment
        this.emptyLayer = emptyLayer
    }

    override fun toJson(): JsonObject = json

    companion object {
        fun parse(imageConfigurationJson: JsonObject): List<History> {
            val histories = mutableListOf<History>()
            val historiesJsons = imageConfigurationJson.array<JsonObject>("history")
            if (historiesJsons != null) {
                for (historyJson in historiesJsons) {
                    val created = historyJson.string("created")
                    val author = historyJson.string("author")
                    val createdBy = historyJson.string("created_by")
                    val comment = historyJson.string("comment")
                    val emptyLayer = historyJson.boolean("empty_layer")

                    histories.add(History(historyJson, created, author, createdBy, comment, emptyLayer))
                }
            }
            return histories
        }
    }
}