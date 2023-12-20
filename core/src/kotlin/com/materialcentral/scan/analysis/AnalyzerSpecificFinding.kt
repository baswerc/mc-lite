package com.materialcentral.scan.analysis

import org.geezer.db.Data
import com.materialcentral.scan.FindingSeverity

abstract class AnalyzerSpecificFinding (
    val analyzerToolId: String,
    var identifier: String,
    var severity: FindingSeverity,
    var title: String?,
    var description: String?,
    var detailsUrls: List<String>,
) : Data() {

}