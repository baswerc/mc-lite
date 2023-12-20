package com.materialcentral.scan.analysis.misconfiguration

import org.geezer.io.ui.FontIcon
import com.materialcentral.scan.FindingSeverity
import com.materialcentral.scan.analysis.AnalyzerSpecificFinding

class MisconfigurationType(
    analyzerToolId: String,
    identifier: String,
    severity: FindingSeverity,
    title: String?,
    description: String?,
    detailsUrls: List<String>,
) : AnalyzerSpecificFinding(analyzerToolId, identifier, severity, title, description, detailsUrls) {
    companion object {
        @JvmField
        val Icon = FontIcon("fa-file-xmark", "f317")
    }
}