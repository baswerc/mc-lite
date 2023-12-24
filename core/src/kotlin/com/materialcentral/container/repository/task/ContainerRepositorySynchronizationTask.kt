package com.materialcentral.container.repository.task

import com.materialcentral.container.client.ContainerRegistryClient
import com.materialcentral.container.image.ContainerImage
import com.materialcentral.container.image.ContainerImagesTable
import com.materialcentral.container.image.task.ContainerImageSynchronizationTask
import com.materialcentral.container.image.task.ContainerImageSynchronizationTasksTable
import com.materialcentral.container.repository.ContainerRepositoriesTable
import com.materialcentral.container.repository.ContainerRepository
import com.materialcentral.container.repository.ContainerRepositoryCoordinates
import org.geezer.io.ui.attribute
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.TagConsumer
import kotlinx.html.div
import org.geezer.io.ui.format
import org.geezer.system.runtime.RuntimeClock
import org.geezer.task.Task
import org.geezer.task.TaskInitializationParameters
import org.geezer.task.TaskType
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList

class ContainerRepositorySynchronizationTask(
    val containerRepositoryId: Long,
    val fullSynchronization: Boolean,
    val parameters: TaskInitializationParameters
) : Task(parameters) {

    constructor(repository: ContainerRepository, fullSynchronization: Boolean) :
            this(repository.id, fullSynchronization, TaskInitializationParameters())

    override val type: TaskType = ContainerRepositorySynchronizationType

    override fun addAttributesAtStart(request: HttpServletRequest, html: TagConsumer<*>, columnSize: String, tableDetails: Boolean) {
        val coordinates = ContainerRepositoryCoordinates.getById(containerRepositoryId)

        html.div(columnSize) {
            html.attribute("Container Registry") {
                coordinates.registry.toLink(html, request)
            }
        }

        html.div(columnSize) {
            html.attribute("Full Synchronization") {
                fullSynchronization.format()
            }
        }
    }

    override fun doTask() {
        val (registry, repository) = ContainerRepositoryCoordinates.getById(containerRepositoryId)
        val allTags = ContainerRegistryClient.getTags(registry, repository)

        repository.lastNewImagesCheckAt = RuntimeClock.transactionAt
        val updatedColumns = mutableListOf<Column<*>>(ContainerRepositoriesTable.lastNewImagesCheckAt)

        if (fullSynchronization) {
            repository.lastFullSynchronizationAt = RuntimeClock.transactionAt
            updatedColumns.add(ContainerRepositoriesTable.lastFullSynchronizationAt)
        }

        if (allTags == null) {
            if (repository.existsInRegistry != false) {
                repository.existsInRegistry = false
                updatedColumns.add(ContainerRepositoriesTable.existsInRegistry)
            }
        } else if (repository.existsInRegistry != true) {
            repository.existsInRegistry = true
            updatedColumns.add(ContainerRepositoriesTable.existsInRegistry)
        }

        if (repository.existsInRegistry == true) {
            val synchronizedImageIds = mutableListOf<Long>()
            ContainerRegistryClient.getTaggedImagesMetadata(registry, repository) { metadata ->
                var imageId = ContainerImagesTable.slice(ContainerImagesTable.id).select { ContainerImagesTable.digest eq metadata.manifest.digest}.singleOrNull()?.let { it[ContainerImagesTable.id] }
                if (imageId == null) {
                    imageId = ContainerImagesTable.create(ContainerImage(repository, metadata.manifest.digest)).id
                    ContainerImageSynchronizationTasksTable.create(ContainerImageSynchronizationTask(imageId))
                    synchronizedImageIds.add(imageId)
                } else if (fullSynchronization) {
                    ContainerImageSynchronizationTasksTable.create(ContainerImageSynchronizationTask(imageId))
                    synchronizedImageIds.add(imageId)
                }
            }

            if (fullSynchronization) {
                var where = (ContainerImagesTable.containerRepositoryId eq containerRepositoryId)
                if (synchronizedImageIds.isNotEmpty()) {
                    where = where and (ContainerImagesTable.id notInList synchronizedImageIds)
                }

                ContainerImagesTable.slice(ContainerImagesTable.id).select(where).forEach { row ->
                    val containerImageId = row[ContainerImagesTable.id]
                    ContainerImageSynchronizationTasksTable.create(ContainerImageSynchronizationTask(containerImageId))
                }
            }
        }

        ContainerRepositoriesTable.update(repository, updatedColumns)
    }
}