package com.materialcentral.scan.analysis.secret

import org.geezer.io.ui.FontIcon
import com.materialcentral.scan.FindingSeverity
import com.materialcentral.scan.analysis.AnalyzerSpecificFinding

class SecretType(
    analyzerToolId: String,
    identifier: String,
    severity: FindingSeverity,
    title: String?,
    description: String?,
    detailsUrls: List<String>
) : AnalyzerSpecificFinding(analyzerToolId, identifier, severity, title, description, detailsUrls) {

    companion object {
        @JvmField
        val Icon = FontIcon("fa-key", "f084")
    }
}