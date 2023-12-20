package com.materialcentral.scan.file

import arrow.core.Either
import arrow.core.right
import com.materialcentral.DataStringsTable
import com.materialcentral.scan.filter.ScanFindingFilter
import com.materialcentral.scan.FindingType
import com.materialcentral.scan.FindingSeverity
import com.materialcentral.scan.Scan
import com.materialcentral.scan.analysis.secret.SecretTypesTable
import com.materialcentral.scan.file.analysis.SecretAnalysisFinding

class SecretScanFinding(
    val secretTypeId: Long,
    override val filePathId: Long,
    val lineNumberStart: Int?,
    val lineNumberEnd: Int?,
    inheritedFinding: Boolean,
    scanId: Long,
    analyzerTypes: List<String>,
    analyzerSeverities: Map<String, FindingSeverity>,
    findingFilterId: Long?
) : FileScanFinding(inheritedFinding, scanId, analyzerTypes, analyzerSeverities, findingFilterId) {
    override val type: FindingType = FindingType.SECRET

    override fun lookupSeverity(): FindingSeverity? {
        return analyzerSeverities.map { it.value }.maxOrNull()
    }

    override fun detailsMatchFilter(filter: ScanFindingFilter): Boolean {
        if (filter.findingPrimaryIdentifierRegexs.isNotEmpty()) {
            val secretType = SecretTypesTable.getById(secretTypeId)
            if (filter.findingPrimaryIdentifierRegexs.none { regex -> regex.matches(secretType.identifier) }) {
                return false
            }
        }

        return true
    }

    companion object {
        fun create(scan: Scan, analysisFinding: SecretAnalysisFinding): Either<String, SecretScanFinding> {
            val type = SecretTypesTable.findUpdateOrCreate(analysisFinding.analyzerId, analysisFinding.identifier, analysisFinding.analyzerSeverity, analysisFinding.title, analysisFinding.description, analysisFinding.detailsUrls)
            val filePathId = DataStringsTable.getOrCreate(analysisFinding.filePath ?: "N/A")
            return SecretScanFindingsTable.create(SecretScanFinding(type.id, filePathId, analysisFinding.lineStartAt, analysisFinding.lineEndAt, analysisFinding.inheritedFinding, scan.id,
                listOf(analysisFinding.analyzerId), mapOf(analysisFinding.analyzerId to analysisFinding.analyzerSeverity), null)).right()
        }
    }
}