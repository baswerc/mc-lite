package com.materialcentral.container.repository

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.container.registry.ContainerRegistriesTable
import org.geezer.db.schema.DataTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase

object ContainerRepositoriesTable : DataTable<ContainerRepository>("container_repositories") {

    val containerRegistryId = long("container_registries_id").referencesWithStandardNameAndIndex(ContainerRegistriesTable.id, ReferenceOption.CASCADE)

    val name = varchar("name", 500)

    val description = description()

    val baseContainerRepositoryId = long("base_container_repository_id").referencesWithStandardNameAndIndex(ContainerRepositoriesTable.id, ReferenceOption.SET_NULL).nullable()

    val baseContainerRepository = bool("base_container_repository")

    val imagesLastSynchronizedAt = long("images_last_synchronized_at").nullable()

    val existsInRegistry = bool("exists_in_registry").nullable()

    val latestImageUploadedAt = long("latest_image_uploaded_At").nullable()

    val addedAt = long("added_at")

    val active = active()

    override fun mapDataToStatement(repository: ContainerRepository, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[containerRegistryId] = repository.containerRegistryId
        statement[baseContainerRepositoryId] = repository.baseContainerRepositoryId
        statement[baseContainerRepository] = repository.baseContainerRepository
        statement[imagesLastSynchronizedAt] = repository.imagesLastSynchronizedAt
        statement[existsInRegistry] = repository.existsInRegistry
        statement[latestImageUploadedAt] = repository.latestImageUploadedAt
        statement[addedAt] = repository.addedAt
        statement[latestImageUploadedAt] = repository.latestImageUploadedAt
    }

    override fun constructData(row: ResultRow): ContainerRepository {
        return ContainerRepository(row[containerRegistryId], row[name], row[description], row[baseContainerRepositoryId], row[baseContainerRepository], row[imagesLastSynchronizedAt],
            row[existsInRegistry], row[latestImageUploadedAt], row[active], row[addedAt])
    }

    fun nameExistsInRegistry(containerRegistryId: Long, name: String): Boolean {
        return rowsExists { (ContainerRepositoriesTable.containerRegistryId eq containerRegistryId) and (ContainerRepositoriesTable.name.lowerCase() eq name) }
    }
}