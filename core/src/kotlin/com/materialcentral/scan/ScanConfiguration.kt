package com.materialcentral.scan

import arrow.core.*
import com.beust.klaxon.JsonObject
import org.geezer.json.Jsonable
import org.geezer.db.schema.JsonObjectDecoder
import com.materialcentral.scan.analysis.AnalysisConfiguration
import com.materialcentral.scan.analysis.AnalysisFindingRetentionCriteria
import org.geezer.toList
import org.geezer.toStringList
import org.jetbrains.exposed.sql.Column
import java.util.SortedSet

class ScanConfiguration(
    val ignoredPaths: List<String>,
    val analysisConfigurations: List<AnalysisConfiguration>,
    val analysisFindingRetentionCriterion: List<AnalysisFindingRetentionCriteria>,
) : Jsonable {

    val findingTypes: SortedSet<FindingType>
        get() = analysisConfigurations.flatMap { it.findingTypes }.toSortedSet()

    constructor() : this(listOf(), listOf(), listOf())

    override fun toJson(): JsonObject {
        return JsonObject().apply {
            this["ignoredPaths"] = ignoredPaths
            this["analysisConfigurations"] = analysisConfigurations.map { it.toJson() }
            this["analysisFindingRetentionCriterion"] = analysisFindingRetentionCriterion.map { it.toJson() }
        }
    }

    override fun toString(): String {
        return toJsonString()
    }

    companion object : JsonObjectDecoder<ScanConfiguration> {
        override fun createDefault(): ScanConfiguration {
            return ScanConfiguration()
        }

        override fun decode(json: JsonObject, column: Column<*>, attributes: Map<String, Any>): Either<String, ScanConfiguration> {
            val ignoredPaths = json.toStringList("ignoredPaths")

            val analyzeConfigurations = json.toList<JsonObject>("analysisConfigurations").map { AnalysisConfiguration.map(it).getOrElse {
                return it.left() }
            }

            val analysisFindingRetentionCriteria = json.toList<JsonObject>("analysisFindingRetentionCriterion").map { AnalysisFindingRetentionCriteria.map(it).getOrElse {
                return it.left() }
            }

            return ScanConfiguration(ignoredPaths, analyzeConfigurations, analysisFindingRetentionCriteria).right()
        }
    }
}