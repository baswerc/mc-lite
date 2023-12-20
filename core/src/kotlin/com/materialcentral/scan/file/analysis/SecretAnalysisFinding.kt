package com.materialcentral.scan.file.analysis

import com.materialcentral.scan.FindingType
import com.materialcentral.scan.FindingSeverity
import com.materialcentral.scan.analysis.AnalysisFinding

class SecretAnalysisFinding(
    val identifier: String,
    val title: String?,
    val description: String?,
    val lineStartAt: Int?,
    val lineEndAt: Int?,
    val detailsUrls: List<String>,
    filePath: String?,
    layerId: String?,
    analyzerId: String,
    severity: FindingSeverity
) : FileAnalysisFinding(filePath, layerId, analyzerId, severity) {

    override val findingType: FindingType = FindingType.SECRET

    override val analyzerSeverity: FindingSeverity
        get() = analyzerSeverity!!

    // Secret findings are unique per analyzer
    override fun associatedWithSameScanFinding(analysisFinding: AnalysisFinding): Boolean {
        return false
    }
}