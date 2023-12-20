package com.materialcentral.scan.file

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.right
import com.chimbori.crux.common.nullIfBlank
import com.materialcentral.DataStringsTable
import com.materialcentral.oss.DigestsTable
import com.materialcentral.oss.OssPackageCoordinates
import com.materialcentral.oss.OssPackageReleasesTable
import com.materialcentral.scan.filter.ScanFindingFilter
import com.materialcentral.scan.FindingType
import com.materialcentral.scan.FindingSeverity
import com.materialcentral.scan.Scan
import com.materialcentral.scan.file.analysis.OssPackageReleaseAnalysisFinding

class OssPackageReleaseScanFinding(
    val ossPackageReleaseId: Long,
    override val filePathId: Long?,
    val sizeBytes: Long?,
    val md5DigestId: Long?,
    val sha1DigestId: Long?,
    val sha256DigestId: Long?,
    var criticalFindings: Int?,
    var highFindings: Int?,
    var mediumFindings: Int?,
    var lowFindings: Int?,
    inheritedFinding: Boolean,
    scanId: Long,
    analyzerIds: List<String>,
    analyzerSeverities: Map<String, FindingSeverity>,
    findingFilterId: Long?
) : FileScanFinding(inheritedFinding, scanId, analyzerIds, analyzerSeverities, findingFilterId) {
    override val type: FindingType = FindingType.OSS_PACKAGE_RELEASE

    override fun detailsMatchFilter(filter: ScanFindingFilter): Boolean {
        if (filter.findingPrimaryIdentifierRegexs.isNotEmpty()) {
            val coordinates = OssPackageCoordinates.findById(ossPackageReleaseId)
            if (coordinates != null && filter.findingPrimaryIdentifierRegexs.none { it.matches(coordinates.purl) }) {
                return false
            }
        }

        return true
    }

    companion object {
        fun create(scan: Scan, analysisFindings: NonEmptyList<OssPackageReleaseAnalysisFinding>): Either<String, OssPackageReleaseScanFinding> {
            val (analyzerIds, analyzerSeverities) = getIdsAndSeverities(analysisFindings)
            val finding = analysisFindings.head

            val ossPackageRelease = OssPackageReleasesTable.findOrCreate(finding.packageType, finding.packageName, finding.packageVersion)

            val filePath = analysisFindings.firstNotNullOfOrNull { it.filePath?.nullIfBlank() }
            val filePathId = filePath?.let { DataStringsTable.getOrCreate(it) }

            val inheritedFinding = analysisFindings.any { it.inheritedFinding }

            val sizeBytes = analysisFindings.firstNotNullOfOrNull { it.sizeBytes }
            val md5Digest = analysisFindings.firstNotNullOfOrNull { it.md5Digest }
            val sha1Digest = analysisFindings.firstNotNullOfOrNull { it.sha1Digest }
            val sha256Digest = analysisFindings.firstNotNullOfOrNull { it.sha256Digest }

            val md5DigestId = md5Digest?.let { DigestsTable.getOrCreate(it) }
            val sha1DigestId = sha1Digest?.let { DigestsTable.getOrCreate(it) }
            val sha256DigestId = sha256Digest?.let { DigestsTable.getOrCreate(it) }

            return OssPackageReleaseScanFindingsTable.create(OssPackageReleaseScanFinding(ossPackageRelease.id,
                filePathId, sizeBytes, md5DigestId, sha1DigestId, sha256DigestId, null, null, null, null, inheritedFinding, scan.id,
                analyzerIds, analyzerSeverities, null)).right()
        }
    }

}