package com.materialcentral.scan.analysis

abstract class Analysis(
    val configuration: AnalysisConfiguration,
    val findings: List<AnalysisFinding>
) {
}