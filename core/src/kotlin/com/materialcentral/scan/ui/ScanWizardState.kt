package com.materialcentral.scan.ui

import arrow.core.*
import com.beust.klaxon.JsonObject
import com.materialcentral.scan.*
import org.geezer.io.set
import org.geezer.io.ui.wizard.WizardAction
import org.geezer.io.ui.wizard.WizardState
import com.materialcentral.scan.analysis.*
import org.geezer.toJsonObject
import org.geezer.toStringList
import jakarta.servlet.http.HttpServletRequest
import org.geezer.routes.RequestParameters

abstract class ScanWizardState(
    val scanTargetSourceType: ScanTargetSourceType,
    val scanTargetSourceId: Long,
    var stage: BuildScanConfigurationStage,
    var scanMedium: ScanMedium?,
    var ignoredPaths: List<String>,
    var selectedAnalyzers: List<Analyzer>,
    var analysisConfigurations: List<AnalysisConfiguration>,
    var analysisFindingRetentionCriterion: List<AnalysisFindingRetentionCriteria>,
    step: Int,
) : WizardState(step) {

    val scanTargetType: ScanTargetType
        get() = scanTargetSourceType.scanTargetType

    open val initialConfigurationPanel: String? = null

    val hasInitialConfiguration: Boolean
        get() = initialConfigurationPanel != null

    val validScanMediums: NonEmptyList<ScanMedium>
        get() {
            return scanTargetSourceType.scanTargetType.scanMediums
        }

    val validAnalyzers: List<Analyzer>
        get() {
            val scanMedium = scanMedium ?: scanTargetSourceType.scanTargetType.scanMediums.head
            return AnalyzerRegistry[scanTargetSourceType.scanTargetType, scanMedium]
        }

    val configuredScanTypes: List<FindingType>
        get() = analysisConfigurations.flatMap { it.findingTypes }.sorted().distinct()

    fun updateCurrentState(request: HttpServletRequest, parameters: RequestParameters, action: WizardAction) {
        val stepOffset = this.step - if (hasInitialConfiguration) 1 else 0

        if (stepOffset == 0) {
            scanMedium = ScanMedium.mapOptional(parameters.getOptionalInt("scanMedium"))
            if (scanMedium == null && action == WizardAction.NEXT) {
                scanMedium = validScanMediums.head
            }
            ignoredPaths = parameters["ignoredPaths"]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: ignoredPaths
        } else if (stepOffset == 1) {
            val validAnalyzers = this.validAnalyzers
            var selectedAnalyzers = parameters.getValues("analyzer").mapNotNull { id -> validAnalyzers.firstOrNull { id == it.id } }
            if (selectedAnalyzers.isEmpty() && action == WizardAction.NEXT) {
                selectedAnalyzers = if (this.selectedAnalyzers.isEmpty()) validAnalyzers else this.selectedAnalyzers
            }

            this.selectedAnalyzers = selectedAnalyzers
        } else {
            val analyzersToConfigure = getAnalyzersToConfigure()
            val lastConfigureAnalyzerStep = 1 + analyzersToConfigure.size
            if (stepOffset <= lastConfigureAnalyzerStep) {
                val analyzerToConfigure = analyzersToConfigure[lastConfigureAnalyzerStep - stepOffset]

                var findingTypes = parameters.getValues("findingType").mapNotNull { FindingType.mapOptional(it.toIntOrNull()) }.filter { analyzerToConfigure.findingTypes.contains(it) }.distinct()
                if (findingTypes.isEmpty() && action == WizardAction.NEXT) {
                    findingTypes = analyzerToConfigure.findingTypes
                }

                val analyzerConfiguration = parameters["analyzerConfiguration"]?.toJsonObject()
                replaceOrAdd(AnalysisConfiguration(analyzerToConfigure.id, findingTypes.sorted(), analyzerConfiguration))
            } else {
                val findingTypesToConfigure = getFindingTypesToConfigure()
                val lastFindingTypeStep = lastConfigureAnalyzerStep + findingTypesToConfigure.size
                if (stepOffset <= lastFindingTypeStep) {
                    val findingTypeToConfigure = findingTypesToConfigure[lastFindingTypeStep - stepOffset]

                    val retainedCriteria = AnalysisFindingGroupingCriteria.mapOptional(parameters.getInt("retainedFindingCriteria")) ?: AnalysisFindingGroupingCriteria.defaultValue
                    val retainedSeverities = if (findingTypeToConfigure.hasSeverities) {
                        var findingSeverities = parameters.getValues("retainedSeverity").mapNotNull { FindingSeverity.mapOptional(it.toIntOrNull()) }.distinct().sorted()
                        if (findingSeverities.isEmpty()) {
                            findingSeverities = FindingSeverity.dataEnumValues.toList()
                        }
                        findingSeverities
                    } else {
                        listOf()
                    }

                    replaceOrAdd(AnalysisFindingRetentionCriteria(findingTypeToConfigure, retainedCriteria, retainedSeverities))
                }
            }
        }
    }

    fun showNextState(request: HttpServletRequest, parameters: RequestParameters, action: WizardAction, confirmHandler:() -> String): String {
        updateStep(action)

        val stepOffset = step - if (hasInitialConfiguration) 1 else 0
        return if (stepOffset == 0) {
            "selectMedium.jsp"
        } else if (stepOffset == 1) {
            "selectAnalyzers.jsp"
        } else {
            val analyzersToConfigure = getAnalyzersToConfigure()
            val lastConfigureAnalyzerStep = 1 + analyzersToConfigure.size
            if (stepOffset <= lastConfigureAnalyzerStep) {
                val analyzerToConfigure = analyzersToConfigure[lastConfigureAnalyzerStep - stepOffset]
                val analysisConfiguration = analysisConfigurations.firstOrNull { it.analyzerId == analyzerToConfigure.id } ?: AnalysisConfiguration(analyzerToConfigure.id, analyzerToConfigure.findingTypes)
                request["analyzer"] = analyzerToConfigure
                request["analysisConfiguration"] = analysisConfiguration
                "configureAnalysis.jsp"
            } else {
                val findingTypesToConfigure = getFindingTypesToConfigure()
                val lastFindingTypeStep = lastConfigureAnalyzerStep + findingTypesToConfigure.size
                if (stepOffset <= lastFindingTypeStep) {
                    val findingTypeToConfigure = findingTypesToConfigure[lastFindingTypeStep - stepOffset]
                    val findingCriteria = analysisFindingRetentionCriterion.firstOrNull { it.type == findingTypeToConfigure } ?: AnalysisFindingRetentionCriteria(findingTypeToConfigure, AnalysisFindingGroupingCriteria.defaultValue, if (findingTypeToConfigure.hasSeverities) FindingSeverity.dataEnumValues.toList() else listOf())

                    request["findingType"] = findingTypeToConfigure
                    request["findingCriteria"] = findingCriteria
                    "configureFindingType.jsp"
                } else {
                    confirmHandler()
                }
            }
        }
    }

    override fun addTo(json: JsonObject) {
        super.addTo(json)
        json["scanTargetSourceType"] = scanTargetSourceType.readableId
        json["scanTargetSourceId"] = scanTargetSourceId
        json["stage"] = stage.id
        json["scanMedium"] = scanMedium?.readableId
        json["ignoredPaths"] = ignoredPaths
        json["selectedAnalyzers"] = selectedAnalyzers.map { it.id }
        json["analysisConfigurations"] = analysisConfigurations.map { it.toJson() }
        json["analysisFindingRetentionCriterion"] = analysisFindingRetentionCriterion.map { it.toJson() }
    }

    fun replaceOrAdd(configuration: AnalysisConfiguration) {
        var configurations = analysisConfigurations.toMutableList()
        configurations.removeIf { it.analyzerId == configuration.analyzerId }
        configurations.add(configuration)
        analysisConfigurations = configurations
    }

    fun replaceOrAdd(configuration: AnalysisFindingRetentionCriteria) {
        var configurations = analysisFindingRetentionCriterion.toMutableList()
        configurations.removeIf { it.type == configuration.type }
        configurations.add(configuration)
        analysisFindingRetentionCriterion = configurations.sortedBy { it.type }
    }

    fun getAnalyzersToConfigure(): List<Analyzer> {
        return selectedAnalyzers.filter { analyzer -> analyzer.findingTypes.size > 1 || analyzer.hasToolSpecificConfiguration }.sortedBy { it.toolName }
    }

    fun getFindingTypesToConfigure(): List<FindingType> {
        return FindingType.dataEnumValues.filter { type -> analysisConfigurations.any { it.findingTypes.contains(type) } }.filter { type -> type.hasSeverities || analysisConfigurations.count { it.findingTypes.contains(type) } > 1 }.sortedBy { it.label }
    }

    fun getSelectedAnalyzersWithType(type: FindingType): List<Analyzer> {
        return selectedAnalyzers.filter { analyzer -> analysisConfigurations.firstOrNull { analyzer.id == it.analyzerId }?.findingTypes?.contains(type) == true }
    }

    companion object {
        class ScanWizardStateProperties(
            val scanTargetSourceType: ScanTargetSourceType,
            val scanTargetSourceId: Long,
            val stage: BuildScanConfigurationStage,
            val scanMedium: ScanMedium?,
            val ignoredPaths: List<String>,
            val selectedAnalyzers: List<Analyzer>,
            val analysisConfigurations: List<AnalysisConfiguration>,
            val analysisFindingRetentionCriteria: List<AnalysisFindingRetentionCriteria>,
            val step: Int
        )

        fun map(json: JsonObject): Either<String, ScanWizardStateProperties> {
            val scanTargetSourceType = ScanTargetSourceType.mapOptionalReadableId(json.string("scanTargetSourceType")) ?: return "Missing valid scanTargetSourceType property.".left()

            val scanTargetSourceId = json.long("scanTargetSourceId") ?: return "Missing parentId property.".left()

            val stage  = BuildScanConfigurationStage.map(json.int("stage")) ?: return "Missing valid stage property.".left()

            val scanMedium = ScanMedium.mapOptionalReadableId(json.string("scanMedium"))

            val ignoredPaths = json.toStringList("ignoredPaths")

            val analyzers = mutableListOf<Analyzer>()
            val analysisConfigurations = mutableListOf<AnalysisConfiguration>()

            if (scanMedium != null) {
                val scanTargetType = scanTargetSourceType.scanTargetType
                val selectedAnalyzersIds = json.array<String>("selectedAnalyzers") ?: listOf()
                if (selectedAnalyzersIds.isNotEmpty()) {
                    val validAnalyzers = AnalyzerRegistry[scanTargetType, scanMedium]
                    if (validAnalyzers.isEmpty()) {
                        return "No valid analyzers found for scan target type ${scanTargetType.label} and scan medium ${scanMedium.label}.".left()
                    }
                    analyzers.addAll(selectedAnalyzersIds.mapNotNull { id -> validAnalyzers.firstOrNull { it.id == id } })
                    if (analyzers.isNotEmpty()) {
                        val analysisConfigurationArray = json.array<JsonObject>("analysisConfigurations") ?: listOf()
                        for (analysisConfigurationJson in analysisConfigurationArray) {
                            val analysisConfiguration = AnalysisConfiguration.map(analysisConfigurationJson).getOrElse { return it.left() }
                            if (analyzers.any { it.id == analysisConfiguration.analyzerId }) {
                                analysisConfigurations.add(analysisConfiguration)
                            }
                        }
                    }
                }
            }

            val analysisFindingRetentionCriteria = json.array<JsonObject>("analysisFindingRetentionCriterion")?.map { AnalysisFindingRetentionCriteria.map(it).getOrElse { return it.left() } } ?: listOf()

            val step = getStep(json)
            return ScanWizardStateProperties(scanTargetSourceType, scanTargetSourceId, stage, scanMedium, ignoredPaths, analyzers, analysisConfigurations, analysisFindingRetentionCriteria, step).right()
        }
    }
}