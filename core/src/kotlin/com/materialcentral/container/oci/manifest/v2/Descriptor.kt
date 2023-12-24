package com.materialcentral.container.oci.manifest.v2

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import org.geezer.json.Jsonable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * https://github.com/opencontainers/image-spec/blob/main/descriptor.md
 */
abstract class Descriptor : Jsonable {
    abstract fun addTo(json: JsonObject)

    /**
     * This REQUIRED property contains the media type of the referenced content. Values MUST comply with RFC 6838, including the naming requirements in its section 4.2.
     *
     * https://github.com/opencontainers/image-spec/blob/main/media-types.md
     */
    val mediaType: String

    /**
     * This REQUIRED property is the digest of the targeted content, conforming to the requirements outlined in Digests. Retrieved content SHOULD be verified against this digest when consumed via untrusted sources.
     */
    val digest: String

    /**
     * This REQUIRED property specifies the size, in bytes, of the raw content. This property exists so that a client will have an expected size for the content before processing. If the length of the retrieved content does not match
     * the specified length, the content SHOULD NOT be trusted.
     */
    val size: Long

    /**
     * This OPTIONAL property specifies a list of URIs from which this object MAY be downloaded. Each entry MUST conform to RFC 3986. Entries SHOULD use the http and https schemes, as defined in RFC 7230.
     */
    val urls: List<String>

    /**
     * This OPTIONAL property contains arbitrary metadata for this descriptor. This OPTIONAL property MUST use the annotation rules.
     */
    val annotations: List<Pair<String, String>>

    val annotationsMap: Map<String, String>
        get() {
            val map = mutableMapOf<String, String>()
            annotations.forEach { map[it.first] = it.second }
            return map
        }

    constructor(parameters: DescriptorParameters) {
        mediaType = parameters.mediaType
        digest = parameters.digest
        size = parameters.size
        urls = parameters.urls
        annotations = parameters.annotations
    }

    final override fun toJson(): JsonObject = json {
        obj(
            "mediaType" to mediaType,
            "digest" to digest,
            "size" to size,
            "urls" to urls,
            "annotations" to JsonObject(annotationsMap),
        ).apply { addTo(this) }
    }

    companion object {

        fun mapDescriptor(json: JsonObject, propertyName: String): Either<String, DescriptorParameters> {
            val mediaType = json.string("mediaType")
            if (mediaType.isNullOrBlank()) {
                return "Manifest object $propertyName has no mediaType property.".left()
            }

            val digest = json.string("digest")
            if (digest.isNullOrBlank()) {
                return "Manifest object $propertyName has no digest property.".left()
            }

            val size = json.long("size")
            if (size == null) {
                return "Manifest object $propertyName has no size property.".left()
            }

            val urls = json.array<String>("urls")?.toList() ?: listOf()
            return DescriptorParameters(mediaType, digest, size, urls, mapAnnotations(json.obj("annotations"))).right()
        }

        fun mapAnnotations(annotationsJson: JsonObject?): List<Pair<String, String>> {
            val annotations = mutableListOf<Pair<String, String>>()
            if (annotationsJson != null) {
                for (property in annotationsJson.keys) {
                    annotationsJson.string(property)?.let { value -> annotations.add(Pair(property, value)) }
                }
            }

            return annotations
        }
    }
}

class DescriptorParameters(val mediaType: String, val digest: String, val size: Long, val urls: List<String> = listOf(), val annotations: List<Pair<String, String>> = listOf())
