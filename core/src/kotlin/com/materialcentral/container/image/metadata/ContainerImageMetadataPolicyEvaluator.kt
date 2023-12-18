package com.materialcentral.container.image.metadata

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.materialcentral.MaterialGroup
import com.materialcentral.MaterialGroupType
import org.geezer.db.db
import com.materialcentral.oss.OssPackageReleasesTable
import com.materialcentral.oss.OssPackagesTable
import com.materialcentral.policy.Policy
import com.materialcentral.policy.PoliciesEvaluator
import com.materialcentral.policy.PolicyEvaluator
import com.materialcentral.policy.PolicyType
import com.materialcentral.policy.violation.*
import com.materialcentral.policy.violation.hold.HoldRule
import com.materialcentral.repository.container.ContainerRepositoriesTable
import com.materialcentral.repository.container.ContainerRepository
import com.materialcentral.container.image.ContainerImagesTable
import com.materialcentral.vulnerability.KnownVulnerabilitiesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object ContainerImageMetadataPolicyEvaluator : PolicyEvaluator {

    const val ParentImageBlockingReasonId = "ParentImage"

    override val materialGroupTypes: Set<MaterialGroupType> = setOf(MaterialGroupType.CONTAINER_REPOSITORY)

    override val policyTypes: Set<PolicyType> = setOf(PolicyType.VULNERABILITY, PolicyType.OSS_PACKAGE, PolicyType.SECRET, PolicyType.MISCONFIGURATION)

    override fun evaluate(containerRepository: MaterialGroup, policy: Policy, policyViolationRetriever:(primaryInfractionId: Long?, secondaryInfractionId: Long?, locationId: Long?) -> Violation, holdRules: List<HoldRule>) {
        require(containerRepository is ContainerRepository) { "Invalid root asset type: ${containerRepository.materialGroupType.label}" }

        db {
            ContainerRepositoriesTable.lockRow(containerRepository) {
                var op = policy.violationCriteria(containerRepository, policy.type)

                op = op and (ContainerImagesTable.containerRepositoryId eq containerRepository.id)

                var query: ColumnSet = ContainerImagesTable
                query = query.innerJoin(ContainerImagesTable, { ContainerRepositoriesTable.id }, { containerRepositoryId })

                if (policy.type.scanBased) {
                    val (metadataTable: ContainerImageMetadataTable<*>, primaryInfractionColumn: Column<Long>, secondaryInfractionColumn: Column<Long>?) = when (policy.type) {
                        PolicyType.VULNERABILITY -> {
                            query = query.innerJoin(ContainerImageKnownVulnerabilitiesTable, { ContainerImagesTable.id }, { ContainerImageKnownVulnerabilitiesTable.containerImageId })
                                .innerJoin(KnownVulnerabilitiesTable, { ContainerImageKnownVulnerabilitiesTable.knownVulnerabilityId }, { id })
                                .innerJoin(OssPackageReleasesTable, { ContainerImageKnownVulnerabilitiesTable.ossPackageReleaseId }, { id })
                                .innerJoin(OssPackagesTable, { OssPackageReleasesTable.ossPackageId }, { id })

                            Triple(ContainerImageKnownVulnerabilitiesTable, ContainerImageKnownVulnerabilitiesTable.knownVulnerabilityId, ContainerImageKnownVulnerabilitiesTable.ossPackageReleaseId)
                        }

                        else -> {
                            throw IllegalArgumentException("Unsupported ${javaClass.kotlin.simpleName} policy violation type: ${policy.type.label}")
                        }
                    }

                    val processViolation = { primaryInfractionId: Long, secondaryInfractionId: Long?, filePathId: Long?, imageIds: NonEmptyList<Long>, inheritedBaseImageIds: List<Long> ->

                        var violation = policyViolationRetriever(primaryInfractionId, secondaryInfractionId, filePathId)

                        if (!violation.waived && !PoliciesEvaluator.onHold(violation, holdRules)) {
                            if (inheritedBaseImageIds.isNotEmpty() && (!violation.blocked || (violation.blockingReasonId == ParentImageBlockingReasonId))) {
                                var parentContainerRepository = ContainerRepositoriesTable.findById(containerRepository.baseContainerRepositoryId)
                                if (parentContainerRepository != null) {
                                    val latestParentImages = ContainerImagesTable.findLatestImagesFor(parentContainerRepository.id)
                                    if (latestParentImages.isNotEmpty()) {
                                        // If any of these parent image ids the latest? If so we need to be blocked.
                                        var blocked = inheritedBaseImageIds.intersect(latestParentImages.map { it.id }.toSet()).isNotEmpty()
                                        if (!blocked) {
                                            // Checked to see if the latest images still has the infraction. If so we still need to be blocked even though this image isn't using the latest parent image..
                                            var op = (ContainerImagesTable.containerRepositoryId eq parentContainerRepository.id) and (ContainerImagesTable.latestInRepository eq true) and
                                                    (primaryInfractionColumn eq primaryInfractionId) and (metadataTable.filePathId eq filePathId)
                                            if (secondaryInfractionColumn != null && secondaryInfractionId != null) {
                                                op = op and (secondaryInfractionColumn eq secondaryInfractionId)
                                            }
                                            blocked = ContainerImagesTable.innerJoin(metadataTable, { id }, { containerImageId }).select(op).any()
                                        }

                                        if (blocked) {
                                            var blockingViolationId = violation.blockingViolationId
                                            if (blockingViolationId == null) {
                                                blockingViolationId = ViolationsTable.findUniqueWhere { (ViolationsTable.violatorId eq parentContainerRepository.id) and
                                                        (ViolationsTable.violatorType eq MaterialGroupType.CONTAINER_REPOSITORY) and
                                                        (ViolationsTable.primaryInfractionId eq primaryInfractionId) and (ViolationsTable.secondaryInfractionId eq secondaryInfractionId)
                                                        (ViolationsTable.locationId eq filePathId)}?.id
                                            }

                                            PoliciesEvaluator.block(violation, parentContainerRepository.id, MaterialGroupType.CONTAINER_REPOSITORY, ParentImageBlockingReasonId, blockingViolationId)
                                        } else if (violation.blocked) {
                                            PoliciesEvaluator.activate(violation)
                                        }
                                    }
                                }
                            }

                            if (violation.resolved) {
                                PoliciesEvaluator.activate(violation)
                            }
                        }

                        for (imageId in imageIds) {
                            ViolationsToMaterialsTable.addIfNecessary(violation.id, ViolationsToMaterialsTable.violationId, imageId, ViolationsToMaterialsTable.materialId)
                        }
                    }

                    var currentPrimaryInfractionId: Long? = null
                    var currentSecondaryInfractionId: Long? = null
                    var currentFilePathId: Long? = null
                    var imageIds = mutableSetOf<Long>()
                    val inheritedBaseImageIds = mutableListOf<Long>()

                    val sliceColumns = mutableListOf(ContainerImagesTable.id, primaryInfractionColumn, metadataTable.filePathId)
                    if (secondaryInfractionColumn != null) {
                        sliceColumns.add(secondaryInfractionColumn)
                    }

                    val orderBy = mutableListOf(primaryInfractionColumn to SortOrder.DESC)
                    if (secondaryInfractionColumn != null) {
                        orderBy.add(secondaryInfractionColumn to SortOrder.DESC)
                    }

                    for (row in query.slice(sliceColumns).select(op).orderBy(*orderBy.toTypedArray())) {
                        val primaryInfractionId = row[primaryInfractionColumn]
                        val secondaryInfractionId = secondaryInfractionColumn?.let { row[it] }
                        val filePathId = row[metadataTable.filePathId] as Long?
                        val imageId = row[ContainerImagesTable.id]

                        if (primaryInfractionId != currentPrimaryInfractionId || secondaryInfractionId != currentSecondaryInfractionId || filePathId != currentFilePathId) {
                            if (currentPrimaryInfractionId != null && currentFilePathId != null && imageIds.isNotEmpty()) {
                                processViolation(currentPrimaryInfractionId, secondaryInfractionId, currentFilePathId, imageIds.toNonEmptyListOrNull()!!, inheritedBaseImageIds)
                            }

                            currentPrimaryInfractionId = primaryInfractionId
                            currentFilePathId = filePathId
                            imageIds.clear()
                            inheritedBaseImageIds.clear()
                        }

                        if (row[metadataTable.inheritedMetadata]) {
                            row[ContainerImagesTable.baseContainerImageId]?.let { inheritedBaseImageIds.add(it) }
                        }
                        imageIds.add(imageId)
                    }

                    if (currentPrimaryInfractionId != null && currentFilePathId != null && imageIds.isNotEmpty()) {
                        processViolation(currentPrimaryInfractionId, currentSecondaryInfractionId, currentFilePathId, imageIds.toNonEmptyListOrNull()!!, inheritedBaseImageIds)
                    }
                }

            }
        }
    }
}