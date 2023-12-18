package com.materialcentral.container.image.metadata

import com.materialcentral.LocationsTable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.oss.OssPackageReleasesTable
import com.materialcentral.vulnerability.KnownVulnerabilitiesTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object ContainerImageKnownVulnerabilitiesTable : ContainerImageMetadataTable<ContainerImageKnownVulnerability>("container_image_known_vulnerabilities") {

    val knownVulnerabilityId = long("known_vulnerability_id").referencesWithStandardNameAndIndex(KnownVulnerabilitiesTable.id, ReferenceOption.CASCADE)

    val ossPackageReleaseId = long("oss_package_release_id").referencesWithStandardNameAndIndex(OssPackageReleasesTable.id, ReferenceOption.CASCADE)

    override val metadataId: Column<Long> = knownVulnerabilityId

    override val filePathId = long("file_path_id").referencesWithStandardNameAndIndex(LocationsTable.id, ReferenceOption.NO_ACTION).nullable()

    override fun mapMetadata(metadata: ContainerImageKnownVulnerability, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[knownVulnerabilityId] = metadata.knownVulnerabilityId
        statement[ossPackageReleaseId] = metadata.ossPackageReleaseId
        statement[filePathId] = metadata.filePathId
    }

    override fun constructData(row: ResultRow): ContainerImageKnownVulnerability {
        return ContainerImageKnownVulnerability(row[knownVulnerabilityId], row[ossPackageReleaseId], row[filePathId], row[containerImageId], row[inheritedMetadata])
    }
}