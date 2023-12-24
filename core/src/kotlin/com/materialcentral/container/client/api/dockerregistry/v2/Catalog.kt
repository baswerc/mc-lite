package com.materialcentral.container.client.api.dockerregistry.v2

import org.geezer.toJsonObjectOrBust
import org.geezer.toStringListOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * List a set of available repositories in the local registry cluster. Does not provide any indication of what may be available upstream. Applications can only determine if a repository is available but not if it is not available.
 *
 * https://docs.docker.com/registry/spec/api/#catalog
 */
class Catalog(val repositories: List<String>, val next: String?) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(javaClass)

        @Throws(IllegalArgumentException::class)
        fun parse(jsonString: String?): Catalog {
            if (jsonString.isNullOrBlank()) {
                throw IllegalArgumentException("No content returned.")
            }

            val json = try {
                jsonString.toJsonObjectOrBust()
            } catch (e: Exception) {
                throw IllegalArgumentException("Catalog response returned invalid JSON content: $jsonString")
            }

            val repositories = json.toStringListOrNull("repositories") ?: throw IllegalArgumentException("Catalog response has no repositories property.")
            val next = json.string("next")
            return Catalog(repositories, next)
        }
    }
}