package com.materialcentral.scan.file

import com.materialcentral.DataStringsTable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.oss.DigestsTable
import com.materialcentral.oss.OssPackageReleasesTable
import com.materialcentral.scan.ScansTable.nullable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object OssPackageReleaseScanFindingsTable : FileScanFindingsTable<OssPackageReleaseScanFinding>("oss_package_release_scan_findings") {

    val ossPackageReleaseId = long("oss_package_release_id").referencesWithStandardNameAndIndex(OssPackageReleasesTable.id, ReferenceOption.CASCADE)

    val filePathId = long("file_path_id").referencesWithStandardNameAndIndex(DataStringsTable.id, ReferenceOption.NO_ACTION).nullable()

    val sizeBytes = long("size_bytes").nullable()

    val md5DigestId = long("md5_digest_id").referencesWithStandardNameAndIndex(DigestsTable.id, ReferenceOption.NO_ACTION).nullable()

    val sha1DigestId = long("sha1_digest_id").nullable()

    val sha256DigestId = long("sha256_digest_id").nullable()

    val criticalFindings = integer("critical_findings").nullable()

    val highFindings = integer("high_findings").nullable()

    val mediumFindings = integer("medium_findings").nullable()

    val lowFindings = integer("low_findings").nullable()


    override fun mapRepositoryFinding(packageReleaseFinding: OssPackageReleaseScanFinding, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[ossPackageReleaseId] = packageReleaseFinding.ossPackageReleaseId
        statement[filePathId] = packageReleaseFinding.filePathId
        statement[sizeBytes] = packageReleaseFinding.sizeBytes
        statement[md5DigestId] = packageReleaseFinding.md5DigestId
        statement[sha1DigestId] = packageReleaseFinding.sha1DigestId
        statement[sha256DigestId] = packageReleaseFinding.sha256DigestId
        statement[criticalFindings] = packageReleaseFinding.criticalFindings
        statement[highFindings] = packageReleaseFinding.highFindings
        statement[mediumFindings] = packageReleaseFinding.mediumFindings
        statement[lowFindings] = packageReleaseFinding.lowFindings
    }

    override fun constructData(row: ResultRow): OssPackageReleaseScanFinding {
        return OssPackageReleaseScanFinding(row[ossPackageReleaseId], row[filePathId], row[sizeBytes], row[md5DigestId], row[sha1DigestId], row[sha256DigestId], row[criticalFindings],
            row[highFindings], row[mediumFindings], row[lowFindings], row[inheritedFinding], row[scanId], row[analyzerFamilyIds],
            row[analyzeFindingSeverities]?.data ?: mapOf(), row[findingFilterId])
    }
}