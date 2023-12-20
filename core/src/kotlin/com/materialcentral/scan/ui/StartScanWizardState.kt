package com.materialcentral.scan.ui

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonBase
import com.beust.klaxon.JsonObject
import com.materialcentral.scan.*
import com.materialcentral.scan.analysis.AnalysisConfiguration
import com.materialcentral.scan.analysis.AnalysisFindingRetentionCriteria
import com.materialcentral.scan.analysis.Analyzer

class StartScanWizardState(
    val scanTargetId: Long,
    scanTargetSourceType: ScanTargetSourceType,
    scanTargetSourceId: Long,
    stage: BuildScanConfigurationStage,
    scanMedium: ScanMedium?,
    ignoredPaths: List<String>,
    selectedAnalyzers: List<Analyzer>,
    analysisConfigurations: List<AnalysisConfiguration>,
    analysisFindingRetentionCriteria: List<AnalysisFindingRetentionCriteria>,
    step: Int
) : ScanWizardState(scanTargetSourceType, scanTargetSourceId, stage, scanMedium, ignoredPaths, selectedAnalyzers, analysisConfigurations, analysisFindingRetentionCriteria, step) {

    constructor(scanTarget: ScanTarget) : this(scanTarget.id, scanTarget.scanTargetSourceType, scanTarget.scanTargetSourceId, BuildScanConfigurationStage.CONFIGURE_SCAN_MEDIUM, null, listOf(), listOf(), listOf(), listOf(), 0)

    override fun toJson(): JsonBase {
        return JsonObject().apply {
            this["scanTargetId"] = scanTargetId
            addTo(this)
        }
    }

    companion object {
        fun map(json: JsonObject): Either<String, StartScanWizardState> {
            val scanTargetId = json.long("scanTargetId") ?: return "Missing scanTargetId property.".left()
            val properties = ScanWizardState.map(json).getOrElse { return it.left() }

            return StartScanWizardState(scanTargetId, properties.scanTargetSourceType, properties.scanTargetSourceId, properties.stage, properties.scanMedium, properties.ignoredPaths, properties.selectedAnalyzers,
                properties.analysisConfigurations, properties.analysisFindingRetentionCriteria, properties.step).right()
        }
    }
}