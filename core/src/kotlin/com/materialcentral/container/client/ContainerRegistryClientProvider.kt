package com.materialcentral.container.client

import arrow.core.NonEmptyList
import com.materialcentral.repository.container.ContainerName
import com.materialcentral.repository.container.ContainerRepository
import com.materialcentral.repository.container.registry.ContainerRegistry
import com.materialcentral.container.client.*
import com.materialcentral.repository.container.registry.oci.configuration.ImageConfiguration
import com.materialcentral.repository.container.registry.oci.manifest.Manifest
import com.materialcentral.repository.container.registry.oci.manifest.ManifestLayer
import java.io.IOException
import java.io.OutputStream

interface ContainerRegistryClientProvider {
    /**
     * Get all repositories in given registry. If the registry does not support the list command null is returned.
     */
    @Throws(IOException::class)
    fun getRepositories(registry: ContainerRegistry): List<String>?

    /**
     * Get all repository tags. If the registry does not support the list tags command null is returned.
     */
    @Throws(IOException::class)
    fun getTags(registry: ContainerRegistry, repository: ContainerRepository): List<TagMetadata>?

    /**
     * Get the image metadata for the image at <registry>/repository:tag or <registry>/repository@digest. Returns null if the image cannot be found.
     */
    @Throws(IOException::class)
    fun getImageMetadata(registry: ContainerRegistry, repository: ContainerRepository, tagOrDigest: String, loadTags: Boolean = true): NonEmptyList<ImageMetadata>?

    /**
     * Process all image metadata for the given repository and registry. Returns true if the repository was found in the registry otherwise false.
     */
    @Throws(IOException::class)
    fun getTaggedImagesMetadata(registry: ContainerRegistry, repository: ContainerRepository, dataProcessor: (ImageMetadata) -> Unit): Boolean

    /**
     * The image manifest or null if 404 is returned.
     */
    @Throws(IOException::class)
    fun getManifest(registry: ContainerRegistry, repository: ContainerRepository, tagOrDigest: String): Manifest?

    @Throws(IOException::class)
    fun getConfiguration(registry: ContainerRegistry, repository: ContainerRepository, configDigest: String): ImageConfiguration?

    @Throws(IOException::class)
    fun downloadLayer(registry: ContainerRegistry, repository: ContainerRepository, layer: ManifestLayer, targetStream: OutputStream, downloadProgressListener: LayerDownloadListener? = null)
}