package com.materialcentral.container.client.api.dockerregistry.v2

import org.geezer.toJsonObjectOrBust
import org.geezer.toStringListOrNull

/**
 * https://docs.docker.com/registry/spec/api/#tags
 */
class TagList(val name: String, val tags: List<String>) {

    companion object {
        @Throws(IllegalArgumentException::class)
        fun parse(jsonString: String?): TagList {
            if (jsonString.isNullOrBlank()) {
                throw IllegalArgumentException("No content returned.")
            }

            val json = try {
                jsonString.toJsonObjectOrBust()
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid JSON content: $jsonString")
            }

            val name = json.string("name") ?: throw IllegalArgumentException("No name property returned.")

            val tags = json.toStringListOrNull("tags") ?: throw IllegalArgumentException("No tags property returned.")

            return TagList(name, tags)
        }
    }
}