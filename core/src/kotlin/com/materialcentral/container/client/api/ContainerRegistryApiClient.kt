package com.materialcentral.container.client.api

import arrow.core.NonEmptyList
import com.materialcentral.container.client.ImageMetadata
import com.materialcentral.container.client.LayerDownloadListener
import com.materialcentral.container.client.TagMetadata
import com.materialcentral.container.oci.configuration.ImageConfiguration
import com.materialcentral.container.oci.manifest.Manifest
import com.materialcentral.container.oci.manifest.ManifestLayer
import com.materialcentral.container.registry.ContainerRegistry
import com.materialcentral.container.repository.ContainerRepository
import java.io.IOException
import java.io.OutputStream

interface ContainerRegistryApiClient {
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
    fun getRepositoryImagesMetadata(registry: ContainerRegistry, repository: ContainerRepository, dataProcessor: (ImageMetadata) -> Unit): Boolean

    /**
     * The image manifest or null if 404 is returned.
     */
    @Throws(IOException::class)
    fun getManifest(registry: ContainerRegistry, repository: ContainerRepository, tagOrDigest: String): Manifest?

    @Throws(IOException::class)
    fun getConfiguration(registry: ContainerRegistry, repository: ContainerRepository, configDigest: String): ImageConfiguration?

    @Throws(IOException::class)
    fun downloadLayer(registry: ContainerRegistry, repository: ContainerRepository, layer: ManifestLayer, targetStream: OutputStream, downloadProgressListener: LayerDownloadListener?)
}