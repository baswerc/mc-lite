package com.materialcentral.container.image.metadata

import com.materialcentral.repository.container.ContainerRepositoriesTable
import com.materialcentral.container.image.ContainerImage
import com.materialcentral.container.image.ContainerImagesTable
import com.materialcentral.scan.*
import com.materialcentral.scan.file.*
import com.materialcentral.scan.schedule.ScanSchedulesTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Responsible for updating the metadata of an image with the latest results of scheduled scans for the image repository.
 */
object ContainerImageMetaSynchronizer : ScanSynchronizationListener<ContainerImage> {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun onScanSynchronized(scan: Scan, findings: List<ScanFinding>, image: ContainerImage) {
        if (scan.scheduleId == null) {
            log.info("Ignoring scan without schedule: $scan for image: $image")
            return
        }

        val repository = ContainerRepositoriesTable.findById(image.containerRepositoryId)
        if (repository == null) {
            log.warn("Unable to retrieve repository for image: $image")
            return
        }

        val repositorySchedules = ScanSchedulesTable.findScanSchedulesFor(repository)
        val latestScans = ScansTable.findLatestScansFor(repositorySchedules)

        if (latestScans.isEmpty()) {
            log.info("Ignoring image: $image with no latest scans.")
            return
        }

        if (latestScans.none { it.id == scan.id }) {
            log.info("Ignoring scan: $scan not in latest scans for image: $image")
            return
        }

        ContainerImagesTable.lockRow(image) {
            for (scanType in FindingType.enumValues) {
                val scansWithType = latestScans.filter { it.configuration.findingTypes.contains(scanType) }
                when (scanType) {
                    FindingType.OSS_PACKAGE_RELEASE -> {
                        val scanPackages = if (scansWithType.isEmpty()) {
                            listOf()
                        } else if (latestScans.size == 1) {
                            findings.filterIsInstance<OssPackageReleaseScanFinding>()
                        } else {
                            OssPackageReleaseScanFindingsTable.findWhere { OssPackageReleaseScanFindingsTable.scanId inList latestScans.map { it.id } and (OssPackageReleaseScanFindingsTable.findingFilterId eq null) }
                        }

                        if (scanPackages.isEmpty()) {
                            val deleted = ContainerImageOssPackageReleasesTable.deleteWhere { ContainerImageOssPackageReleasesTable.containerImageId eq image.id }
                            if (deleted > 0) {
                                log.info("Deleted: $deleted OSS packages in image: $image because no OSS packages were found in latest scans.")
                            }
                        } else {
                            val imagePackages = ContainerImageOssPackageReleasesTable.findWhere { ContainerImageOssPackageReleasesTable.containerImageId eq image.id }

                            for (scanPackage in scanPackages) {
                                val imagePackage = imagePackages.firstOrNull { it.ossPackageReleaseId ==  scanPackage.ossPackageReleaseId && it.filePathId == scanPackage.filePathId}

                                if (imagePackage == null) {
                                    val added = ContainerImageOssPackageReleasesTable.create(ContainerImageOssPackageRelease(scanPackage.ossPackageReleaseId, scanPackage.filePathId,
                                        scanPackage.sizeBytes, scanPackage.md5DigestId, scanPackage.sha1DigestId, scanPackage.sha256DigestId, scanPackage.criticalFindings,
                                        scanPackage.highFindings, scanPackage.mediumFindings, scanPackage.lowFindings, image.id, scanPackage.inheritedFinding))
                                    log.info("Added OSS package: $added to image: $image")
                                }
                            }

                            for (imagePackage in imagePackages) {
                                val scanPackage = scanPackages.firstOrNull { it.ossPackageReleaseId ==  imagePackage.ossPackageReleaseId && it.filePathId == imagePackage.filePathId}
                                if (scanPackage == null) {
                                    log.info("Deleting OSS package: $imagePackage for image: $image")
                                    ContainerImageOssPackageReleasesTable.delete(imagePackage)
                                }
                            }

                        }
                    }

                    FindingType.KNOWN_VULNERABILITY -> {
                        val scanVulnerabilities = if (scansWithType.isEmpty()) {
                            listOf()
                        } else if (latestScans.size == 1) {
                            findings.filterIsInstance<KnownVulnerabilityScanFinding>()
                        } else {
                            KnownVulnerabilityScanFindingsTable.findWhere { KnownVulnerabilityScanFindingsTable.scanId inList latestScans.map { it.id } and (KnownVulnerabilityScanFindingsTable.findingFilterId eq null) }
                        }

                        if (scanVulnerabilities.isEmpty()) {
                            val deleted = ContainerImageKnownVulnerabilitiesTable.deleteWhere { ContainerImageKnownVulnerabilitiesTable.containerImageId eq image.id }
                            if (deleted > 0) {
                                log.info("Deleted: $deleted known vulnerabilities from image: $image because no known vulnerabilities were found in latest scans.")
                            }
                        } else {
                            val imageVulnerabilities = ContainerImageKnownVulnerabilitiesTable.findWhere { ContainerImageKnownVulnerabilitiesTable.containerImageId eq image.id }

                            for (scanVulnerability in scanVulnerabilities) {
                                val imageVulnerability = imageVulnerabilities.firstOrNull { it.knownVulnerabilityId == scanVulnerability.knownVulnerabilityId
                                        && it.ossPackageReleaseId ==  scanVulnerability.ossPackageReleaseId && it.filePathId == scanVulnerability.filePathId}

                                if (imageVulnerability == null) {
                                    val added = ContainerImageKnownVulnerabilitiesTable.create(ContainerImageKnownVulnerability(scanVulnerability.knownVulnerabilityId, scanVulnerability.ossPackageReleaseId,
                                        scanVulnerability.filePathId, image.id, scanVulnerability.inheritedFinding))
                                    log.info("Added know vulnerability: $added to image: $image")
                                }
                            }

                            for (imageVulnerability in imageVulnerabilities) {
                                val scanVulnerability = scanVulnerabilities.firstOrNull { it.knownVulnerabilityId == imageVulnerability.knownVulnerabilityId
                                        && it.ossPackageReleaseId ==  imageVulnerability.ossPackageReleaseId && it.filePathId == imageVulnerability.filePathId}

                                if (scanVulnerability == null) {
                                    log.info("Deleting known vulnerability: $imageVulnerability for image: $image")
                                    ContainerImageKnownVulnerabilitiesTable.delete(imageVulnerability)
                                }
                            }

                        }
                    }

                    FindingType.SECRET -> {
                        val scanSecrets = if (scansWithType.isEmpty()) {
                            listOf()
                        } else if (latestScans.size == 1) {
                            findings.filterIsInstance<SecretScanFinding>()
                        } else {
                            SecretScanFindingsTable.findWhere { SecretScanFindingsTable.scanId inList latestScans.map { it.id } and (SecretScanFindingsTable.findingFilterId eq null) }
                        }

                        if (scanSecrets.isEmpty()) {
                            val deleted = ContainerImageSecretsTable.deleteWhere { ContainerImageSecretsTable.containerImageId eq image.id }
                            if (deleted > 0) {
                                log.info("Deleted: $deleted secret from image: $image because no secrets were found in latest scans.")
                            }
                        } else {
                            val imageSecrets = ContainerImageSecretsTable.findWhere { ContainerImageSecretsTable.containerImageId eq image.id }

                            for (scanSecret in scanSecrets) {
                                val imageSecret = imageSecrets.firstOrNull { it.secretTypeId == scanSecret.secretTypeId && it.filePathId == scanSecret.filePathId}

                                if (imageSecret == null) {
                                    val added = ContainerImageSecretsTable.create(ContainerImageSecret(scanSecret.secretTypeId, scanSecret.filePathId, image.id, scanSecret.inheritedFinding))
                                    log.info("Added secret: $added to image: $image")
                                }
                            }

                            for (imageSecret in imageSecrets) {
                                val scanSecret = scanSecrets.firstOrNull { it.secretTypeId == imageSecret.secretTypeId && it.filePathId == imageSecret.filePathId}
                                if (scanSecret == null) {
                                    log.info("Deleting secret: $imageSecret for image: $image")
                                    ContainerImageSecretsTable.delete(imageSecret)
                                }
                            }

                        }
                    }

                    FindingType.MISCONFIGURATION -> {
                        val scanMisconfigurations = if (scansWithType.isEmpty()) {
                            listOf()
                        } else if (latestScans.size == 1) {
                            findings.filterIsInstance<MisconfigurationScanFinding>()
                        } else {
                            MisconfigurationScanFindingsTable.findWhere { MisconfigurationScanFindingsTable.scanId inList latestScans.map { it.id } and (MisconfigurationScanFindingsTable.findingFilterId eq null) }
                        }

                        if (scanMisconfigurations.isEmpty()) {
                            val deleted = ContainerImageMisconfigurationsTable.deleteWhere { ContainerImageMisconfigurationsTable.containerImageId eq image.id }
                            if (deleted > 0) {
                                log.info("Deleted: $deleted misconfigurations from image: $image because no misconfigurations where found from latest scans.")
                            }
                        } else {
                            val imageMisconfigurations = ContainerImageMisconfigurationsTable.findWhere { ContainerImageMisconfigurationsTable.containerImageId eq image.id }

                            for (scanMisconfiguration in scanMisconfigurations) {
                                val imageMisconfiguration = imageMisconfigurations.firstOrNull { it.misconfigurationTypeId == scanMisconfiguration.misconfigurationTypeId && it.filePathId == scanMisconfiguration.filePathId}

                                if (imageMisconfiguration == null) {
                                    val added = ContainerImageMisconfigurationsTable.create(ContainerImageMisconfiguration(scanMisconfiguration.misconfigurationTypeId, scanMisconfiguration.filePathId, image.id, scanMisconfiguration.inheritedFinding))
                                    log.info("Added misconfiguration: $added to image: $image")
                                }
                            }

                            for (imageMisconfiguration in imageMisconfigurations) {
                                val scanSecret = scanMisconfigurations.firstOrNull { it.misconfigurationTypeId == imageMisconfiguration.misconfigurationTypeId && it.filePathId == imageMisconfiguration.filePathId}
                                if (scanSecret == null) {
                                    log.info("Deleting misconfiguration: $imageMisconfiguration for image: $image")
                                    ContainerImageMisconfigurationsTable.delete(imageMisconfiguration)
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}