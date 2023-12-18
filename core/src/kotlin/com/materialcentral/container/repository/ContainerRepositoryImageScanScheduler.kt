package com.materialcentral.container.repository

import com.materialcentral.LocationsTable
import com.materialcentral.container.image.ContainerImageDeploymentsTable
import com.materialcentral.container.image.ContainerImageTagsTable
import com.materialcentral.container.image.ContainerImagesTable
import com.materialcentral.container.registry.ContainerRegistriesTable
import com.materialcentral.scan.Scan
import com.materialcentral.scan.ScanTargetType
import com.materialcentral.scan.ScansTable
import com.materialcentral.scan.schedule.ScanSchedule
import org.geezer.db.schema.ilike
import org.geezer.system.runtime.RuntimeClock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

object ContainerRepositoryImageScanScheduler {
    fun scheduleImageScan(containerRepository: ContainerRepository, schedule: ScanSchedule) {
        if (!schedule.scanAllMaterials && !schedule.scanDefaultMaterial && schedule.scanMaterialsInEnvironmentIds.isEmpty() && schedule.scanMaterialNamePatterns.isEmpty()) {
            return
        }

        var imageFilter: Op<Boolean> = ContainerImagesTable.containerRepositoryId eq containerRepository.id
        if (!schedule.scanAllMaterials) {
            var imageFilters = mutableListOf<Op<Boolean>>()
            if (schedule.scanDefaultMaterial) {
                imageFilter = imageFilter and (ContainerImagesTable.latestInRepository eq true)
            }

            if (schedule.scanMaterialsInEnvironmentIds.isNotEmpty()) {
                imageFilter = imageFilter and exists(ContainerImageDeploymentsTable.slice(ContainerImageDeploymentsTable.id).select { (ContainerImageDeploymentsTable.containerImageId eq ContainerImagesTable.id) and
                        (ContainerImageDeploymentsTable.undeployedAt eq null) and (ContainerImageDeploymentsTable.environmentId inList schedule.scanMaterialsInEnvironmentIds)})
            }

            if (schedule.scanMaterialNamePatterns.isNotEmpty()) {
                imageFilter = imageFilter and exists(ContainerImageTagsTable.slice(ContainerImageTagsTable.id).select { (ContainerImageTagsTable.removedAt eq null) and
                        (ContainerImageTagsTable.containerImageId eq ContainerImagesTable.id) and (ContainerImageTagsTable.value ilike schedule.scanMaterialNamePatterns)})
            }
        }

        var containerImageCandidates = ContainerImagesTable.slice(ContainerImagesTable.id, ContainerImagesTable.name).select(imageFilter).map { it[ContainerImagesTable.id] to it[ContainerImagesTable.name] }.toMutableList()

        if (containerImageCandidates.isEmpty()) {
            return
        }

        val s = ScansTable.alias("s")
        val rows = ScansTable.select {(ScansTable.scheduleId eq schedule.id) and (ScansTable.scanTargetId inList containerImageCandidates.map { it.first }) and
                // Make sure this is the latest initial scan for each image.
                (ScansTable.createdAt inSubQuery(s.slice(s[ScansTable.createdAt]).select { (s[ScansTable.scheduleId] eq schedule.id) and (s[ScansTable.scanTargetId] eq ScansTable.scanTargetId) }
                    .orderBy(ScansTable.createdAt, SortOrder.DESC).limit(1)) )}
        val minHoursBetweenScans = schedule.minimumHoursBetweenScans ?: ScanSchedule.defaultMinHoursBetweenScans()
        val repositoryHostname = ContainerRegistriesTable.slice(ContainerRegistriesTable.hostname).select{ ContainerRegistriesTable.id eq containerRepository.containerRegistryId }.singleOrNull()?.let { it[ContainerRegistriesTable.hostname] } ?: ""
        for (row in rows) {
            val containerImageId = row[ContainerImagesTable.id]
            val containerImageName = containerImageCandidates.firstOrNull { it.first == containerImageId }
            if (containerImageName != null) {
                val endedAt = row[ScansTable.endedAt]
                if (endedAt != null) {
                    if (minHoursBetweenScans >= RuntimeClock.hoursAgo(endedAt)) {
                        containerImageCandidates.removeIf { it.first == containerImageId }
                        val scanNameId = LocationsTable.getOrCreate("$repositoryHostname/${containerRepository.name}/${containerImageName}")
                        ScansTable.create(Scan(containerRepository.id, ScanTargetType.CONTAINER_IMAGE, containerImageId, scanNameId, schedule.medium, schedule.scanConfiguration, schedule.id))
                    } else {
                        containerImageCandidates.removeIf { it.first == containerImageId }                    }
                } else {
                    containerImageCandidates.removeIf { it.first == containerImageId }                }
            }
        }

        for (index in containerImageCandidates.indices.reversed()) {
            val (imageId, imageName) = containerImageCandidates[index]
            containerImageCandidates.removeAt(index)
            val scanNameId = LocationsTable.getOrCreate("$repositoryHostname/${containerRepository.name}/${imageName}")
            ScansTable.create(Scan(containerRepository.id, ScanTargetType.CONTAINER_IMAGE, imageId, scanNameId, schedule.medium, schedule.scanConfiguration, schedule.id))
        }

        schedule.scanMaterialsInEnvironmentIds
    }
}