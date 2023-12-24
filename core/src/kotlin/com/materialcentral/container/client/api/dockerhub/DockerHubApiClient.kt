package com.materialcentral.container.client.api.dockerhub

import com.materialcentral.container.client.api.dockerregistry.v2.BaseDockerRegistryV2ApiClient
import com.materialcentral.container.registry.ContainerRegistry
import com.materialcentral.container.repository.ContainerName
import com.materialcentral.container.repository.ContainerRepository

/**
 * TODO - https://docs.docker.com/docker-hub/api/latest/
 */
object DockerHubApiClient : BaseDockerRegistryV2ApiClient()  {

    const val DockerHubDefaultNamespace = "library"

    override fun getRepositories(registry: ContainerRegistry): List<String>? {
        return null
    }

    override fun getNamespaceRepositoryPath(repository: ContainerRepository): String {
        val name = ContainerName(repository.name)
        return if (name.namespace.isNullOrBlank()) {
            "$DockerHubDefaultNamespace/${name.repository}"
        } else {
            name.namespaceRepository
        }
    }

}