package com.materialcentral.container.oci.manifest

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import org.geezer.json.Jsonable
import com.materialcentral.container.oci.manifest.v1.V1Manifest
import com.materialcentral.container.oci.manifest.v2.V2Manifest
import com.materialcentral.container.oci.manifest.v2.list.ManifestList
import org.geezer.causeMessage
import org.geezer.digest.Digester
import org.geezer.toJsonObjectOrBust
import java.text.SimpleDateFormat

interface Manifest : Jsonable {
    val content: String

    /**
     * The schema version number.
     */
    val schemaVersion: ManifestSchemaVersion

    val mediaType: String

    val digest: String

    val lastModifiedAt: Long?

    val layers: List<ManifestLayer>

    val totalSizeBytes: Long?
        get() = layers.mapNotNull { it.size }.let { if (it.isEmpty()) null else it.sum() }

    override fun toJson(): JsonObject = content.toJsonObjectOrBust()

    companion object {
        const val DockerContentDigestHeader = "Docker-Content-Digest"

        const val ETagHeader = "ETag"

        const val LastModifiedHeader = "Last-Modified"

        const val ContentTypeHeader = "Content-Type"

        val ManifestMediaTypes = listOf(ManifestList.MediaTypes, V2Manifest.MediaTypes, V1Manifest.MediaTypes).flatten()

        const val LastModifiedFormat = "EEE, dd MMM yyyy HH:mm:ss zzz"

        fun getDigestFromHeaders(headers: List<Pair<String, String>>): String? {
            var digest = headers.firstOrNull { it.first.equals(DockerContentDigestHeader, true) }?.second
            if (digest.isNullOrBlank()) {
                digest = headers.firstOrNull { it.first.equals(ETagHeader, true) }?.second?.replace("\"", "")
            }

            return digest
        }

        fun getLastModifiedAtFromHeaders(headers: List<Pair<String, String>>): Either<String, Long?> {
            val lastModifiedHeader = headers.firstOrNull { it.first.equals(LastModifiedHeader, true) }?.second
            if (lastModifiedHeader == null) {
                return null.right()
            }

            return try {
                SimpleDateFormat(LastModifiedFormat).parse(lastModifiedHeader).time.right()
            } catch (e: Exception) {
                "Unable to parse $LastModifiedHeader header $lastModifiedHeader with format: $LastModifiedFormat due to: ${e.causeMessage}".left()
            }
        }


        @Throws(IllegalArgumentException::class)
        fun parseManifest(jsonString: String?, headers: List<Pair<String, String>> = listOf(),
                          mediaType: String? = headers.firstOrNull { it.first.equals(ContentTypeHeader, true) }?.second, digest: String? = getDigestFromHeaders(headers),
                          lastModifiedAt: Long? = getLastModifiedAtFromHeaders(headers).getOrNull()): Either<String, Manifest> {
            if (jsonString.isNullOrBlank()) {
                return "No content returned.".left()
            }

            val json = try {
                jsonString.toJsonObjectOrBust()
            } catch (e: Exception) {
                return "Invalid JSON: $jsonString due to: ${e.causeMessage}".left()
            }

            if (mediaType.isNullOrBlank()) {
                return "Manifest response has no $ContentTypeHeader header in response.".left()
            }

            var digest = digest
            if (digest.isNullOrBlank()) {
                digest = "sha256:${Digester.sha256(jsonString)}"
            }

            return if (V2Manifest.MediaTypes.any { it.equals(mediaType, true) }) {
                V2Manifest.parse(jsonString, json, digest, mediaType, lastModifiedAt)
            } else if (ManifestList.MediaTypes.any { it.equals(mediaType, true) }) {
                ManifestList.parse(jsonString, json, digest, mediaType, lastModifiedAt)
            }
            else if (V1Manifest.MediaTypes.any { it.equals(mediaType, true) }) {
                V1Manifest.parse(jsonString, json, digest, mediaType, lastModifiedAt)
            } else {
                "Unknown manifest content type $mediaType for response\n$jsonString.".left()
            }
        }
    }
}