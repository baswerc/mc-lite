package com.materialcentral.container.image.metadata

import com.materialcentral.LocationsTable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.oss.DigestsTable
import com.materialcentral.oss.OssPackageReleasesTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object ContainerImageOssPackageReleasesTable : ContainerImageMetadataTable<ContainerImageOssPackageRelease>("container_image_oss_package_releases") {

    val ossPackageReleaseId = long("oss_package_release_id").referencesWithStandardNameAndIndex(OssPackageReleasesTable.id, ReferenceOption.CASCADE)

    override val filePathId = long("file_path_id").referencesWithStandardNameAndIndex(LocationsTable.id, ReferenceOption.NO_ACTION).nullable()

    val sizeBytes = long("size_bytes").nullable()

    val md5DigestId = long("md5_digest_id").referencesWithStandardNameAndIndex(DigestsTable.id, ReferenceOption.NO_ACTION).nullable()

    val sha1DigestId = long("sha1_digest_id").nullable()

    val sha256DigestId = long("sha256_digest_id").nullable()

    val criticalFindings = integer("critical_findings").nullable()

    val highFindings = integer("high_findings").nullable()

    val mediumFindings = integer("medium_findings").nullable()

    val lowFindings = integer("low_findings").nullable()

    override val metadataId: Column<Long> = ossPackageReleaseId

    override fun mapMetadata(metadata: ContainerImageOssPackageRelease, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[ossPackageReleaseId] = metadata.ossPackageReleaseId
        statement[filePathId] = metadata.filePathId
        statement[md5DigestId] = metadata.md5DigestId
        statement[sha1DigestId] = metadata.sha1DigestId
        statement[sha256DigestId] = metadata.sha256DigestId
        statement[criticalFindings] = metadata.criticalFindings
        statement[highFindings] = metadata.highFindings
        statement[mediumFindings] = metadata.mediumFindings
        statement[lowFindings] = metadata.lowFindings
    }

    override fun constructData(row: ResultRow): ContainerImageOssPackageRelease {
        return ContainerImageOssPackageRelease(row[ossPackageReleaseId], row[filePathId], row[sizeBytes], row[md5DigestId], row[sha1DigestId], row[sha256DigestId], row[criticalFindings],
            row[highFindings], row[mediumFindings], row[lowFindings], row[containerImageId], row[inheritedMetadata])
    }
}