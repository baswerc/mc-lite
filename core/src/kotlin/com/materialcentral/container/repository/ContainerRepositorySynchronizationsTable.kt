package com.materialcentral.container.repository

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.job.JobInitializationParameters
import com.materialcentral.job.JobsTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object ContainerRepositorySynchronizationsTable : JobsTable<ContainerRepositorySynchronization>("container_repository_synchronizations") {

    val containerRepositoryId = long("container_repository_id").referencesWithStandardNameAndIndex(ContainerRepositoriesTable.id, ReferenceOption.CASCADE)

    val reevaluateLayers = bool("reevaluate_layers")

    val synchronizeUntaggedImages = bool("synchronize_untagged_images")

    override fun mapJob(synchronization: ContainerRepositorySynchronization, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[containerRepositoryId] = synchronization.containerRepositoryId
        statement[reevaluateLayers] = synchronization.reevaluateLayers
        statement[synchronizeUntaggedImages] = synchronization.synchronizeUntaggedImages
    }

    override fun constructAndLink(row: ResultRow, parameters: JobInitializationParameters): ContainerRepositorySynchronization {
        return ContainerRepositorySynchronization(row[containerRepositoryId], row[reevaluateLayers], row[synchronizeUntaggedImages], parameters)
    }
}