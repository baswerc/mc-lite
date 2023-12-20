package com.materialcentral.scan.file.analysis

import com.materialcentral.scan.analysis.AnalysisFinding
import com.materialcentral.scan.FindingSeverity

abstract class FileAnalysisFinding(
    var filePath: String?,
    var layerId: String?,
    analyzerId: String,
    severity: FindingSeverity?
) : AnalysisFinding(analyzerId, severity) {

    var inheritedFinding: Boolean = false

    override fun associatedWithSameScanFinding(analysisFinding: AnalysisFinding): Boolean {
        return super.associatedWithSameScanFinding(analysisFinding) &&
                ((analysisFinding is FileAnalysisFinding) && (
                        // Some analyzers might not include file path on things like OS packages so if one leaves the file path this might still be equal.
                        ((filePath.isNullOrBlank() || analysisFinding.filePath.isNullOrBlank()) && layerId == analysisFinding.layerId)
                                || (!filePath.isNullOrBlank() && filePath == analysisFinding.filePath)))
    }

    fun removeFilePathPrefix(pathPrefix: String) {
        filePath = filePath?.removePrefix(pathPrefix)
    }
}