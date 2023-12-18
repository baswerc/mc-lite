package com.materialcentral.container.registry

import org.geezer.db.DataEnum
import org.geezer.db.DataEnumType

enum class RegistryAuthenticationType(override val id: Int, override val label: String) : DataEnum {
    ANONYMOUS(1, "Anonymous"),
    USERNAME_PASSWORD(2, "Username and Password"),
    BEARER_TOKEN(3, "Bearer Token"),
    DOCKERHUB_ANONYMOUS(4, "Dockerhub Anonymous");

    companion object : DataEnumType<RegistryAuthenticationType> {
        override val dataEnumValues: Array<RegistryAuthenticationType> = values()
    }
}