package com.materialcentral.container.image

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.job.JobInitializationParameters
import com.materialcentral.job.JobState
import com.materialcentral.job.JobsTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

object ContainerImageSynchronizationsTable : JobsTable<ContainerImageSynchronization>("container_image_synchronizations") {

    val containerImageId = long("container_image_id").referencesWithStandardNameAndIndex(ContainerImagesTable.id, ReferenceOption.CASCADE)

    override fun mapJob(synchronization: ContainerImageSynchronization, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[containerImageId] = synchronization.containerImageId
    }

    override fun constructAndLink(row: ResultRow, parameters: JobInitializationParameters): ContainerImageSynchronization {
        return ContainerImageSynchronization(row[containerImageId], parameters)
    }

    override fun createRunEstimationsFilters(job: ContainerImageSynchronization): List<Op<Boolean>> {
        val filters = mutableListOf<Op<Boolean>>()
        filters.add((containerImageId eq job.containerImageId) and (state eq JobState.SUCCEEDED))
        filters.addAll(super.createRunEstimationsFilters(job))
        return filters
    }

}