package com.materialcentral.container.repository

import org.geezer.HasLongId
import org.geezer.HasName
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.HasIcon
import org.geezer.io.ui.Linkable
import com.materialcentral.container.registry.ContainerRegistriesTable
import com.materialcentral.container.registry.ContainerRegistry
import com.materialcentral.container.ui.ContainerRepositoryUiController
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import kotlin.reflect.KFunction

class ContainerRepositoryCoordinates(
    val repository: ContainerRepository,
    val registry: ContainerRegistry
) : Linkable, HasName, HasIcon, HasLongId {

    val containerName: ContainerName by lazy { registry.createContainerName(repository) }

    override val route: KFunction<*> = ContainerRepositoryUiController::getRepository

    override val id: Long = repository.id

    override val name: String
        get() = containerName.toString()

    override val icon: FontIcon = ContainerRepository.Icon

    operator fun component1(): ContainerRegistry {
        return registry
    }

    operator fun component2(): ContainerRepository {
        return repository
    }

    companion object {
        fun getById(repositoryId: Long): ContainerRepositoryCoordinates {
            return findById(repositoryId) ?: throw IllegalArgumentException("Container repository with id: $repositoryId does not exists.")
        }

        fun findById(repositoryId: Long): ContainerRepositoryCoordinates? {
            return ContainerRepositoriesTable.innerJoin(ContainerRegistriesTable, { containerRegistryId }, { id }).select { ContainerRepositoriesTable.id eq repositoryId }.singleOrNull()?.let {
                ContainerRepositoryCoordinates(ContainerRepositoriesTable.map(it), ContainerRegistriesTable.map(it))
            }
        }
    }
}