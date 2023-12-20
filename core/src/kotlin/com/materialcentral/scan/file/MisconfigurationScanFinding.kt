package com.materialcentral.scan.file

import arrow.core.Either
import arrow.core.right
import com.materialcentral.DataStringsTable
import com.materialcentral.scan.filter.ScanFindingFilter
import com.materialcentral.scan.FindingType
import com.materialcentral.scan.FindingSeverity
import com.materialcentral.scan.Scan
import com.materialcentral.scan.analysis.misconfiguration.MisconfigurationTypesTable
import com.materialcentral.scan.file.analysis.MisconfigurationAnalysisFinding

class MisconfigurationScanFinding(
    val misconfigurationTypeId: Long,
    override val filePathId: Long,
    val lineNumberStart: Int?,
    val lineNumberEnd: Int?,
    val resolutionId: Long?,
    inheritedFinding: Boolean,
    scanId: Long,
    analyzerIds: List<String>,
    analyzerSeverities: Map<String, FindingSeverity>,
    findingFilterId: Long?
) : FileScanFinding(inheritedFinding, scanId, analyzerIds, analyzerSeverities, findingFilterId) {

    override val type: FindingType = FindingType.MISCONFIGURATION

    override fun lookupSeverity(): FindingSeverity? {
        return analyzerSeverities.map { it.value }.maxOrNull()
    }

    override fun detailsMatchFilter(filter: ScanFindingFilter): Boolean {
        if (filter.findingPrimaryIdentifierRegexs.isNotEmpty()) {
            val misconfigurationType = MisconfigurationTypesTable.getById(misconfigurationTypeId)
            if (filter.findingPrimaryIdentifierRegexs.none { regex -> regex.matches(misconfigurationType.identifier) } ) {
                return false
            }
        }

        return true
    }

    companion object {
        fun create(scan: Scan, analysisFinding: MisconfigurationAnalysisFinding): Either<String, MisconfigurationScanFinding> {
            val type = MisconfigurationTypesTable.findUpdateOrCreate(analysisFinding.analyzerId, analysisFinding.identifier, analysisFinding.analyzerSeverity, analysisFinding.title, analysisFinding.description, analysisFinding.detailsUrls)
            val filePathId = DataStringsTable.getOrCreate(analysisFinding.filePath ?: "N/A")
            val resolutionId = analysisFinding.resolution?.let { DataStringsTable.getOrCreate(it) }
            return MisconfigurationScanFindingsTable.create(MisconfigurationScanFinding(type.id, filePathId, analysisFinding.lineStartAt, analysisFinding.lineEndAt, resolutionId, analysisFinding.inheritedFinding, scan.id,
                listOf(analysisFinding.analyzerId), mapOf(analysisFinding.analyzerId to analysisFinding.analyzerSeverity), null)).right()
        }
    }
}