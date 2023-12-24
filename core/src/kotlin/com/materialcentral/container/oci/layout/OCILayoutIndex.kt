package com.materialcentral.container.oci.layout

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import org.geezer.json.Jsonable
import com.materialcentral.container.oci.manifest.v2.Descriptor
import org.geezer.causeMessage
import org.geezer.toJsonObjectOrBust
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OCILayoutIndex(val schemaVersion: Int = 2, val mediaType: String? = MediaType, val manifests: List<IndexManifest>, val annotations: List<Pair<String, String>> = listOf()) :
    Jsonable {

    override fun toJson(): JsonObject = json {
        obj(
            "schemaVersion" to schemaVersion,
            "mediaType" to mediaType,
            "manifests" to manifests.map { it.toJson() }
        )
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(javaClass)

        const val MediaType = "application/vnd.oci.image.index.v1+json"

        fun parse(jsonString: String?): OCILayoutIndex? {
            if (jsonString.isNullOrBlank()) {
                log.warn("No image index JSON content")
                return null
            }


            val json = try {
                jsonString.toJsonObjectOrBust()
            } catch (e: Exception) {
                log.warn("Unable to parse image index JSON content $jsonString due to exception: ${e.causeMessage}")
                return null
            }

            val schemaVersion = json.int("schemaVersion")
            if (schemaVersion == null) {
                log.warn("No schemaVersion property on index manifest platform JSON: ${json.toJsonString()}")
                return null
            }

            val mediaType = json.string("mediaType")

            val manifests = json.array<JsonObject>("manifests")?.mapNotNull { IndexManifest.map(it) } ?: listOf()
            val annotations = Descriptor.mapAnnotations(json.obj("annotations"))

            return OCILayoutIndex(schemaVersion, mediaType, manifests, annotations)
        }
    }
}