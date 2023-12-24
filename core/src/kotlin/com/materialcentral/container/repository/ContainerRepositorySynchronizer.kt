package com.materialcentral.container.repository

import com.materialcentral.job.JobCreator
import com.materialcentral.job.JobInitializationParameters
import com.materialcentral.job.JobState
import com.materialcentral.job.JobWorker
import com.materialcentral.container.image.*
import com.materialcentral.container.image.task.ContainerImageSynchronizer
import com.materialcentral.container.registry.client.ContainerRegistryClient
import com.materialcentral.container.registry.client.TagMetadata
import com.materialcentral.container.repository.task.ContainerRepositorySynchronizationTask
import com.materialcentral.container.repository.task.ContainerRepositorySynchronizationTasksTable
import org.geezer.system.runtime.IntProperty
import org.geezer.system.runtime.RuntimeClock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList

object ContainerRepositorySynchronizer : JobWorker<ContainerRepositorySynchronizationTask>(ContainerRepositorySynchronizationTask::class, ContainerRepositorySynchronizationTasksTable), JobCreator{

    val minutesBetweenSynchronization = IntProperty("ContainerRepositoryMinMinutesBetweenSynchronization", 60)

    val minutesBetweenFullSynchronization = IntProperty("ContainerRepositoryMinMinutesBetweenFulSynchronization", 60 * 12)

    val minSecondsBetweenJobCreationAttemptProperty: IntProperty = IntProperty("ContainerImageSyncMinMinutesCheck",5)

    override val minSecondsBetweenJobCreationAttempt: Int
        get() = minSecondsBetweenJobCreationAttemptProperty()

    override fun produceNeededJobs() {
        val maxLastSynchronized = RuntimeClock.nowMinusMinutes(minutesBetweenSynchronization())
        val containerRepositoryIds = ContainerRepositoriesTable.slice(ContainerRepositoriesTable.id).select {
            (ContainerRepositoriesTable.active eq true) and
                    ((ContainerRepositoriesTable.imagesLastSynchronizedAt eq null) or (ContainerRepositoriesTable.imagesLastSynchronizedAt lessEq maxLastSynchronized)) and
                    notExists(ContainerRepositorySynchronizationTasksTable.select { (ContainerRepositorySynchronizationTasksTable.containerRepositoryId eq ContainerRepositoriesTable.id) and
                            (ContainerRepositorySynchronizationTasksTable.state inList JobState.activeStates)})}.map { it[ContainerRepositoriesTable.id] }

        for (containerRepositoryId in containerRepositoryIds) {
            val maxCreatedAtCol = ContainerRepositorySynchronizationTasksTable.createdAt.max()
            val maxCreatedAt = ContainerRepositorySynchronizationTasksTable.slice(maxCreatedAtCol).select { (ContainerRepositorySynchronizationTasksTable.containerRepositoryId eq containerRepositoryId) and
                    (ContainerRepositorySynchronizationTasksTable.fullSynchronization eq true) and (ContainerRepositorySynchronizationTasksTable.synchronizeUntaggedImages)}.singleOrNull()?.let { it[maxCreatedAtCol] }

            var reevaluateLayers = false
            var synchronizeUntaggedImages = false
            if (maxCreatedAt == null || (RuntimeClock.minutesAgo(maxCreatedAt) >= minutesBetweenFullSynchronization())) {
                reevaluateLayers = true
                synchronizeUntaggedImages = true
            }

            ContainerRepositorySynchronizationTasksTable.create(ContainerRepositorySynchronizationTask(containerRepositoryId, reevaluateLayers, synchronizeUntaggedImages, JobInitializationParameters()))
        }
    }
    override fun work(job: ContainerRepositorySynchronizationTask) {
        val (registry, repository) = ContainerRepositoryCoordinates.getById(job.containerRepositoryId)
        log.info("Starting container image synchronization for: ${repository.name}")
        try {
            val synchronizationStartedAt = RuntimeClock.now
            repository.imagesLastSynchronizedAt = synchronizationStartedAt

            val allTags = ContainerRegistryClient.getTags(registry, repository)

            if (allTags == null) {
                if (repository.existsInRegistry != false) {
                    repository.existsInRegistry = false
                    ContainerRepositoriesTable.update(repository, ContainerRepositoriesTable.existsInRegistry)
                    return
                }
            } else {
                repository.existsInRegistry = true
            }

            val imageIdsByTags = mutableListOf<Pair<TagMetadata, MutableList<Long>>>()

            val imageIdsWithTags = mutableListOf<Long>()
            ContainerRegistryClient.getTaggedImagesMetadata(registry, repository) { metadata ->
                var image = ContainerImagesTable.findByDigest(metadata.manifest.digest)
                var newImage = false
                if (image == null) {
                    image = ContainerImagesTable.create(ContainerImage(repository, metadata.manifest.digest))
                    newImage = true
                }

                imageIdsWithTags.add(image.id)

                ContainerImageSynchronizer.synchronizeProperties(image, metadata)
                ContainerImageSynchronizer.synchronizeTags(image, metadata, allTags, synchronizationStartedAt)

                if (newImage || job.fullSynchronization) {
                    ContainerImageSynchronizer.synchronizeLayers(image, metadata.manifest)
                }

                for (tag in metadata.tags) {
                    val imageIds = imageIdsByTags.firstOrNull { it.first.value == tag.value }?.second
                    if (imageIds == null) {
                        imageIdsByTags.add(tag to mutableListOf(image.id))
                    } else {
                        imageIds.add(image.id)
                    }
                }
            }

            if (job.synchronizeUntaggedImages) {
                ContainerImagesTable.select { (ContainerImagesTable.containerRepositoryId eq repository.id) and (ContainerImagesTable.id notInList imageIdsWithTags) }.forEach { row ->
                    val image = ContainerImagesTable.map(row)
                    val metadata = ContainerRegistryClient.getImageMetadata(registry, repository, image, false)
                    ContainerImageSynchronizer.synchronizeProperties(image, metadata)
                    if (metadata != null) {
                        ContainerImageSynchronizer.synchronizeTags(image, metadata, allTags, synchronizationStartedAt)
                        if (job.fullSynchronization) {
                            ContainerImageSynchronizer.synchronizeLayers(image, metadata.manifest)
                        }
                    }
                }
            }

            for ((tagMeta, imageIds) in imageIdsByTags) {
                val (tag, timestamp) = tagMeta
                val removedAt = timestamp ?: synchronizationStartedAt
                ContainerImageTagsTable.update({(ContainerImageTagsTable.value eq tag) and (ContainerImageTagsTable.removedAt eq null) and
                        (ContainerImageTagsTable.containerImageId notInList imageIds)}) { statement ->
                    statement[ContainerImageTagsTable.removedAt] = removedAt
                }
            }

            var untagFilter: Op<Boolean> = exists(ContainerImagesTable.select { (ContainerImagesTable.id eq ContainerImageTagsTable.id) and (ContainerImagesTable.containerRepositoryId eq repository.id) }) and
                    (ContainerImageTagsTable.removedAt eq null)

            if (!allTags.isNullOrEmpty()) {
                untagFilter = untagFilter and (ContainerImageTagsTable.value notInList allTags.map { it.value })
            }

            ContainerImageTagsTable.update({ untagFilter} ) { statement ->
                statement[removedAt] = synchronizationStartedAt
            }

            val max = ContainerImagesTable.createdAt.max()
            val maxCreatedAt = ContainerImagesTable.slice(max).select { (ContainerImagesTable.containerRepositoryId eq repository.id) and (ContainerImagesTable.deletedFromRepository eq false) }.singleOrNull()?.let { it[max] }
            if (maxCreatedAt == null) {
                repository.latestImageUploadedAt = null
            } else {
                repository.latestImageUploadedAt = maxCreatedAt

                ContainerImagesTable.update({ (ContainerImagesTable.containerRepositoryId eq repository.id) and (ContainerImagesTable.deletedFromRepository eq false) and (ContainerImagesTable.createdAt eq maxCreatedAt)}) {
                    it[latestInRepository] = true
                }

                ContainerImagesTable.update({ (ContainerImagesTable.containerRepositoryId eq repository.id) and (ContainerImagesTable.createdAt neq maxCreatedAt)}) {
                    it[latestInRepository] = false
                }
            }

            ContainerRepositoriesTable.update(repository,
                ContainerRepositoriesTable.imagesLastSynchronizedAt,
                ContainerRepositoriesTable.existsInRegistry,
                ContainerRepositoriesTable.latestImageUploadedAt
            )

        } finally {
            log.info("Completed container image synchronization for: ${repository.name}")
        }
    }
}