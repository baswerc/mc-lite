package com.materialcentral.container.client.api

import arrow.core.NonEmptyList
import com.materialcentral.repository.container.ContainerName
import com.materialcentral.repository.container.ContainerRepository
import com.materialcentral.repository.container.registry.ContainerRegistry
import com.materialcentral.repository.container.registry.ContainerRegistryType
import com.materialcentral.container.client.ContainerRegistryClientProvider
import com.materialcentral.container.client.ImageMetadata
import com.materialcentral.container.client.LayerDownloadListener
import com.materialcentral.container.client.TagMetadata
import com.materialcentral.container.client.api.dockerhub.DockerHubApiClient
import com.materialcentral.container.client.api.dockerregistry.v2.DockerRegistryV2ApiClient
import com.materialcentral.repository.container.registry.oci.configuration.ImageConfiguration
import com.materialcentral.repository.container.registry.oci.manifest.Manifest
import com.materialcentral.repository.container.registry.oci.manifest.ManifestLayer
import java.io.IOException
import java.io.OutputStream

object ContainerRegistryApiClientProvider : ContainerRegistryClientProvider {
    override fun getRepositories(registry: ContainerRegistry): List<String>? {
        return pickApiClient(registry).getRepositories(registry)
    }

    override fun getTags(registry: ContainerRegistry, repository: ContainerRepository): List<TagMetadata>? {
        return pickApiClient(registry).getTags(registry, repository)
    }

    override fun getImageMetadata(registry: ContainerRegistry, repository: ContainerRepository, tagOrDigest: String, loadTags: Boolean): NonEmptyList<ImageMetadata>? {
        return pickApiClient(registry).getImageMetadata(registry, repository, tagOrDigest, loadTags)
    }

    override fun getTaggedImagesMetadata(registry: ContainerRegistry, repository: ContainerRepository, dataProcessor: (ImageMetadata) -> Unit): Boolean {
        return pickApiClient(registry).getRepositoryImagesMetadata(registry, repository, dataProcessor)
    }

    override fun getManifest(registry: ContainerRegistry, repository: ContainerRepository, tagOrDigest: String): Manifest? {
        return pickApiClient(registry).getManifest(registry, repository, tagOrDigest)
    }

    override fun getConfiguration(registry: ContainerRegistry, repository: ContainerRepository, configDigest: String): ImageConfiguration? {
        return pickApiClient(registry).getConfiguration(registry, repository, configDigest)
    }

    @Throws(IOException::class)
    override fun downloadLayer(registry: ContainerRegistry, repository: ContainerRepository, layer: ManifestLayer, targetStream: OutputStream, downloadProgressListener: LayerDownloadListener?) {
        pickApiClient(registry).downloadLayer(registry, repository, layer, targetStream, downloadProgressListener)
    }

    fun pickApiClient(registry: ContainerRegistry): ContainerRegistryApiClient {
        return when (registry.type) {
            ContainerRegistryType.DOCKER_HUB -> DockerHubApiClient
            ContainerRegistryType.DOCKER_REGISTRY_V2 -> DockerRegistryV2ApiClient

        }
    }
}