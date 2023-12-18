package com.materialcentral.container.registry

import org.geezer.db.DataEnum
import org.geezer.db.DataEnumType

enum class ContainerRegistryType(override val id: Int, override val label: String) : DataEnum {
    DOCKER_REGISTRY_V2(0, "Docker Registry (API V2)"),
    DOCKER_HUB(1, "DockerHub")
    ;

    companion object : DataEnumType<ContainerRegistryType> {
        override val dataEnumValues: Array<ContainerRegistryType> = values()

    }

}