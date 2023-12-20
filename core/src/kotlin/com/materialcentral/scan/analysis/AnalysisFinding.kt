package com.materialcentral.scan.analysis

import com.materialcentral.scan.FindingSeverity
import com.materialcentral.scan.FindingType

abstract class AnalysisFinding (
    val analyzerId: String,
    open val analyzerSeverity: FindingSeverity?,
) {
    abstract val findingType: FindingType

    open fun associatedWithSameScanFinding(analysisFinding: AnalysisFinding): Boolean {
        return findingType == analysisFinding.findingType
    }
}