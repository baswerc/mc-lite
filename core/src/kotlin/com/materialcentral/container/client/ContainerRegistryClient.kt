package com.materialcentral.container.client

import arrow.core.NonEmptyList
import com.materialcentral.repository.container.ContainerRepository
import com.materialcentral.repository.container.image.ContainerImage
import com.materialcentral.repository.container.registry.ContainerRegistry
import com.materialcentral.container.client.api.ContainerRegistryApiClientProvider
import com.materialcentral.repository.container.registry.oci.configuration.ImageConfiguration
import com.materialcentral.repository.container.registry.oci.manifest.Manifest
import com.materialcentral.repository.container.registry.oci.manifest.ManifestLayer
import com.materialcentral.repository.container.registry.oci.manifest.v2.V2Manifest
import org.geezer.system.runtime.AppRuntime
import java.io.IOException
import java.io.OutputStream

object ContainerRegistryClient : ContainerRegistryClientProvider {
    private var provider: ContainerRegistryClientProvider = ContainerRegistryApiClientProvider

    fun runWithProvider(provider: ContainerRegistryClientProvider, block:() -> Unit) {
        AppRuntime.assertInUnitTest()

        val originalProvider = this.provider
        this.provider = provider
        try {
            block()
        } finally {
            this.provider = originalProvider
        }
    }

    /**
     * Get all repositories in given registry. If the registry does not support the list command null is returned.
     */
    @Throws(IOException::class)
    override fun getRepositories(registry: ContainerRegistry): List<String>? {
        return provider.getRepositories(registry)
    }

    /**
     * Get all repository tags. If the registry does not support the list tags command null is returned.
     */
    @Throws(IOException::class)
    override fun getTags(registry: ContainerRegistry, repository: ContainerRepository): List<TagMetadata>? {
        return provider.getTags(registry, repository)
    }

    /**
     * Get the image metadata for the image at <registry>/repository:tag or <registry>/repository@digest. Returns null if the image cannot be found.
     */
    @Throws(IOException::class)
    fun getImageMetadata(registry: ContainerRegistry, repository: ContainerRepository, image: ContainerImage, loadTags: Boolean = true): ImageMetadata? {
        return getImageMetadata(registry, repository, image.digest, loadTags)?.firstOrNull()
    }

    /**
     * Get the image metadata for the image at <registry>/repository:tag or <registry>/repository@digest. Returns null if the image cannot be found. Can be multi-value if the tag
     * or digest provided is a multi-architecture image.
     */
    @Throws(IOException::class)
    override fun getImageMetadata(registry: ContainerRegistry, repository: ContainerRepository, tagOrDigest: String, loadTags: Boolean): NonEmptyList<ImageMetadata>? {
        return provider.getImageMetadata(registry, repository, tagOrDigest, loadTags)
    }

    /**
     * Process all metadata for tagged images in the repository. Returns true if the repository was found in the registry otherwise false.
     */
    @Throws(IOException::class)
    override fun getTaggedImagesMetadata(registry: ContainerRegistry, repository: ContainerRepository, imageMetadataProcessor: (ImageMetadata) -> Unit): Boolean {
        return provider.getTaggedImagesMetadata(registry, repository, imageMetadataProcessor)
    }

    /**
     * The image manifest or null if 404 is returned.
     */
    @Throws(IOException::class)
    fun getManifest(registry: ContainerRegistry, repository: ContainerRepository, image: ContainerImage): Manifest? {
        return getManifest(registry, repository, image.digest)
    }

    /**
     * The image manifest or null if 404 is returned.
     */
    @Throws(IOException::class)
    override fun getManifest(registry: ContainerRegistry, repository: ContainerRepository, tagOrDigest: String): Manifest? {
        return provider.getManifest(registry, repository, tagOrDigest)
    }

    fun getConfiguration(registry: ContainerRegistry, repository: ContainerRepository, image: ContainerImage): ImageConfiguration? {
        val manifest = getManifest(registry, repository, image) ?: return null
        return if (manifest is V2Manifest) {
            getConfiguration(registry, repository, manifest.config.digest)
        } else {
            null
        }
    }

    @Throws(IOException::class)
    override fun getConfiguration(registry: ContainerRegistry, repository: ContainerRepository, configDigest: String): ImageConfiguration? {
        return provider.getConfiguration(registry, repository, configDigest)
    }

    @Throws(IOException::class)
    override  fun downloadLayer(registry: ContainerRegistry, repository: ContainerRepository, layer: ManifestLayer, targetStream: OutputStream, downloadProgressListener: LayerDownloadListener?) {
        return provider.downloadLayer(registry, repository, layer, targetStream, downloadProgressListener)
    }
}