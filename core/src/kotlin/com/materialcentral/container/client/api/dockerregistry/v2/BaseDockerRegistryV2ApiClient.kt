package com.materialcentral.container.client.api.dockerregistry.v2

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import org.geezer.io.ui.isUrl
import com.materialcentral.container.client.*
import com.materialcentral.container.client.api.BaseRegistryApiClient
import com.materialcentral.container.client.api.ContainerRegistryApiParseException
import com.materialcentral.container.oci.configuration.ImageConfiguration
import com.materialcentral.container.oci.manifest.Manifest
import com.materialcentral.container.oci.manifest.ManifestLayer
import com.materialcentral.container.oci.manifest.v2.V2Manifest
import com.materialcentral.container.oci.manifest.v2.list.ManifestList
import com.materialcentral.container.registry.ContainerRegistry
import com.materialcentral.container.repository.ContainerName
import com.materialcentral.container.repository.ContainerRepository
import org.geezer.system.runtime.IntProperty
import org.geezer.causeMessage
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.OutputStream

/**
 * https://docs.docker.com/registry/spec/api/
 */
open class BaseDockerRegistryV2ApiClient : BaseRegistryApiClient() {

    /**
     * https://docs.docker.com/registry/spec/api/#catalog
     */
    override fun getRepositories(registry: ContainerRegistry): List<String>? {
        return makeRegistryGetRequest(registry, "_catalog") { response ->
            var repositories = mutableSetOf<String>()
            val catalog = try {
                Catalog.parse(response.body?.string())
            } catch (e: Exception) {
                throw ContainerRegistryApiParseException("Invalid catalog response from $registry with error: ${e.message}")
            }
            repositories.addAll(catalog.repositories)

            var nextUrl = catalog.next
            val visitedUrls = mutableSetOf<String>()
            while (nextUrl != null && !visitedUrls.contains(nextUrl)) {
                visitedUrls.add(nextUrl)
                val catalog = makeRegistryGetRequest(registry, nextUrl) { response ->
                    try {
                        Catalog.parse(response.body?.string())
                    } catch (e: Exception) {
                        throw ContainerRegistryApiParseException("Invalid catalog response from $registry to url: ${response.request.url} with error: ${e.message}")
                    }
                }

                if (catalog == null) {
                    break
                } else {
                    repositories.addAll(catalog.repositories)
                    nextUrl = catalog.next
                }
            }

            repositories.toList()
        }
    }

    /**
     * https://docs.docker.com/registry/spec/api/#tags
     */
    override fun getTags(registry: ContainerRegistry, repository: ContainerRepository): List<TagMetadata>? {
        return makeRegistryGetRequest(registry, "${getNamespaceRepositoryPath(repository)}/tags/list", repository) { response ->
            try {
                TagList.parse(response.body?.string()).tags.map { TagMetadata(it, null) }
            } catch (e: Exception) {
                throw ContainerRegistryApiParseException("Invalid tag list response from $registry to url: ${response.request.url} with error: ${e.message}")
            }
        }
    }

    override fun getImageMetadata(registry: ContainerRegistry, repository: ContainerRepository, tagOrDigest: String, loadTags: Boolean): NonEmptyList<ImageMetadata>? {
        val tagsWithManifest = if (loadTags) getTagsWithManifest(registry, repository) else listOf()
        if (tagsWithManifest == null) {
            return null
        }

        val imageManifest = getManifest(registry, repository, tagOrDigest)
        if (imageManifest == null) {
            return null
        }

        val metadata = mutableListOf<ImageMetadata>()
        val manifestToMetadata = {manifest: Manifest, manifestListDigest: String? ->
            val builder = ImageMetadata.Builder(manifest)
            for ((tag, tagManifest) in tagsWithManifest) {
                if (tagManifest.digest == manifest.digest) {
                    builder.addTag(tag)
                } else if (tagManifest is ManifestList) {
                    for (platformManifest in tagManifest.manifests) {
                        val childManifest = getManifest(registry, repository, platformManifest.digest)
                        if (childManifest != null && childManifest.digest == manifest.digest) {
                            builder.addTag(tag)
                            break
                        }
                    }
                }
            }

            if (manifest is V2Manifest) {
                builder.addConfiguration(getConfiguration(registry, repository, manifest.config.digest))
            }
            builder.build()
        }

        if (imageManifest is ManifestList) {
            for (platformManifest in imageManifest.manifests) {
                val childManifest = getManifest(registry, repository, platformManifest.digest)
                if (childManifest != null) {
                    metadata.add(manifestToMetadata(childManifest, imageManifest.digest))
                }
            }
        } else {
            metadata.add(manifestToMetadata(imageManifest, null))
        }

        return if (metadata.isEmpty()) null else metadata.toNonEmptyListOrNull()!!
    }

    override fun getRepositoryImagesMetadata(registry: ContainerRegistry, repository: ContainerRepository, dataProcessor: (ImageMetadata) -> Unit): Boolean {
        val tagsWithManifest = getTagsWithManifest(registry, repository)?.toMutableList()
        if (tagsWithManifest == null) {
            return false
        }

        val manifestToMetadata = {manifest: Manifest, tags: List<TagMetadata> ->
            val builder = ImageMetadata.Builder(manifest)
            for (tag in tags) {
                builder.addTag(tag)
            }

            if (manifest is V2Manifest) {
                builder.addConfiguration(getConfiguration(registry, repository, manifest.config.digest))
            }
            builder.build()
        }

        while (tagsWithManifest.isNotEmpty()) {
            val (tag, manifest) = tagsWithManifest.removeAt(0)
            val tags = mutableListOf(tag)
            for ((i, tagWithManifest) in tagsWithManifest.withIndex().reversed()) {
                if (tagWithManifest.second == manifest) {
                    tags.add(tagWithManifest.first)
                    tagsWithManifest.removeAt(i)
                }
            }

            if (manifest is ManifestList) {
                for (platformManifest in manifest.manifests) {
                    val childManifest = getManifest(registry, repository, platformManifest.digest)
                    val childTags = mutableListOf<TagMetadata>().apply { addAll(tags) }
                    if (childManifest != null) {
                        for ((i, tagWithManifest) in tagsWithManifest.withIndex().reversed()) {
                            if (tagWithManifest.second.digest == childManifest.digest) {
                                childTags.add(tagWithManifest.first)
                                tagsWithManifest.removeAt(i)
                            }
                        }

                        dataProcessor(manifestToMetadata(childManifest, childTags))
                    }
                }
            } else {
                dataProcessor(manifestToMetadata(manifest, tags))
            }
        }

        return true
    }

    /**
     * https://docs.docker.com/registry/spec/api/#manifest
     */
    override fun getManifest(registry: ContainerRegistry, repository: ContainerRepository, tagOrDigest: String): Manifest? {
        return makeRegistryGetRequest(registry, "${repository.name}/manifests/${tagOrDigest}", repository, acceptTypes = Manifest.ManifestMediaTypes) { response ->
            when (val result = Manifest.parseManifest(response.body?.string(), getResponseHeaders(response))) {
                is Either.Left -> {
                    throw ContainerRegistryApiParseException(result.value)
                }

                is Either.Right -> {
                    result.value
                }
            }
        }
    }

    /**
     * https://docs.docker.com/registry/spec/api/#blob
     */
    override fun getConfiguration(registry: ContainerRegistry, repository: ContainerRepository, configDigest: String): ImageConfiguration? {
        return makeRegistryGetRequest(registry,"${getNamespaceRepositoryPath(repository)}/blobs/$configDigest", acceptTypes = ImageConfiguration.MediaTypes, repository = repository, expectedCodes = listOf(200, 404)) { response ->
            when (val result = ImageConfiguration.parse(response.body?.string(), response.header(DockerContentDigestHeader))) {
                is Either.Left -> {
                    throw ContainerRegistryApiParseException(result.value)
                }

                is Either.Right -> {
                    result.value
                }
            }
        }
    }

    /**
     * https://docs.docker.com/registry/spec/api/#blob
     */
    override fun downloadLayer(registry: ContainerRegistry, repository: ContainerRepository, layer: ManifestLayer, targetStream: OutputStream, downloadProgressListener: LayerDownloadListener?) {
        return targetStream.use {

            val builder = Request.Builder()
            layer.mediaType?.let { builder.addHeader("Accept", it) }

            downloadProgressListener?.onLayerDownloadStart(layer.digest)
            var bytesDownloaded = 0L
            var totalBytes: Long? = null
            try {
                makeRegistryGetRequest(registry, "${getNamespaceRepositoryPath(repository)}/blobs/${layer.digest}", repository) { response ->
                    val expectedSize = layer.size
                    totalBytes = response.headers[ContentLengthHeader]?.toLongOrNull()
                    val totalBytes = totalBytes
                    if (totalBytes == null) {
                        log.error("No $ContentLengthHeader header provided in response.")
                        throw IllegalArgumentException("No $ContentLengthHeader header provided in response.")
                    } else if (expectedSize != null && totalBytes != expectedSize) {
                        log.error("Expected layer size of $expectedSize did not match Content-Length of $totalBytes.")
                        throw IllegalArgumentException("Expected layer size of $expectedSize did not match Content-Length of $totalBytes.")
                        false
                    } else {
                        val inStream = response.body?.byteStream()
                        if (inStream == null) {
                            throw IOException("Download layer response contained no data.")
                        } else {
                            inStream.use {
                                val buffer = ByteArray(bufferSize())
                                while (true) {
                                    var read = try { inStream.read(buffer) } catch (e: IOException) {
                                        log.error("Layer download stream closed before complete.", e)
                                        throw e
                                    }
                                    if (read == -1) {
                                        throw IOException("Layer download stream closed before complete.")
                                    } else {
                                        try {
                                            targetStream.write(buffer, 0, read)
                                            bytesDownloaded += read
                                            downloadProgressListener?.onLayerDownloadProgress(layer.digest, bytesDownloaded, totalBytes)
                                            if (bytesDownloaded >= totalBytes) {
                                                downloadProgressListener?.onLayerDownloadComplete(layer.digest, bytesDownloaded)
                                                break
                                            }
                                        } catch (e: IOException) {
                                            log.error("Unable to copy layer to target stream due to ${e.causeMessage}")
                                            throw e
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                downloadProgressListener?.onLayerDownloadError(layer.digest, bytesDownloaded, totalBytes)
                throw e
            }
        }
    }

    fun getTagsWithManifest(registry: ContainerRegistry, repository: ContainerRepository): List<Pair<TagMetadata, Manifest>>? {
        val tags = getTags(registry, repository) ?: return null
        val tagsWithManifest = mutableListOf<Pair<TagMetadata, Manifest>>()

        for (tag in tags) {
            val manifest = getManifest(registry, repository, tag.value)
            if (manifest != null) {
                tagsWithManifest.add(tag to manifest)
            }
        }
        return tagsWithManifest
    }

    open fun getNamespaceRepositoryPath(repository: ContainerRepository): String {
        return repository.name
    }

    override fun toRootUrl(registry: ContainerRegistry): String {
        return "${super.toRootUrl(registry)}/v2"
    }

    protected fun <T> makeRegistryGetRequest(registry: ContainerRegistry, fullUrlOrPath: String, repository: ContainerRepository? = null,
                                             name: ContainerName? = if (repository != null) ContainerName(registry.hostname, repository.name) else null, expectedCodes: List<Int> = listOf(200),
                                             acceptTypes: List<String> = listOf(), head: Boolean = false, responseProcessor: ((response: Response) -> T?)): T? {

        val url = if (fullUrlOrPath.isUrl) {
            fullUrlOrPath
        } else {
            "${toRootUrl(registry)}/$fullUrlOrPath"
        }
        val builder = setupRequest(registry, name, Request.Builder()).url(url)
        if (acceptTypes.isNotEmpty()) {
            builder.addHeader("Accept", acceptTypes.joinToString(", "))
        }

        if (head) {
            builder.head()
        }

        return httpClient.newCall(builder.build()).execute().use { response ->
            if (response.code in expectedCodes) {
                responseProcessor(response)
            } else if (response.code == 404) {
                null
            } else {
                throw RegistryErrorException(response)
            }
        }
    }

    companion object {
        val bufferSize = IntProperty("DockerRegistryBufferSize", 1024 * 1024)

        const val DockerHubRegistryHostname = "registry-1.docker.io"

        const val DockerHubDefaultNamespace = "library"

        /**
         * When a 200 OK or 401 Unauthorized response is returned, the “Docker-Distribution-API-Version” header should be set to “registry/2.0”. Clients may require this header value to determine if the endpoint serves this API.
         * When this header is omitted, clients may fallback to an older API version.
         */
        const val DockerDistributionApiVersionHeader = "Docker-Distribution-API-Version"

        const val DockerDistributionApiVersion = "registry/2.0"

        const val DockerContentDigestHeader = "Docker-Content-Digest"

        const val ContentLengthHeader = "Content-Length"
    }

}