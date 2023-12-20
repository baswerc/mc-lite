package com.materialcentral.scan.analysis

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonBase
import com.beust.klaxon.JsonObject
import com.materialcentral.scan.FindingSeverity
import com.materialcentral.scan.FindingType
import org.geezer.json.Jsonable
import org.geezer.toStringListOrNull

class AnalysisFindingRetentionCriteria(
    val type: FindingType,
    var groupingCriteria: AnalysisFindingGroupingCriteria,
    var retainedSeverities: List<FindingSeverity>
) : Jsonable {

    constructor(type: FindingType) : this(type, AnalysisFindingGroupingCriteria.defaultValue, FindingSeverity.enumValues.toList())

    override fun toJson(): JsonBase {
        return JsonObject().apply {
            this["type"] = type.readableId
            this["groupingCriteria"] = groupingCriteria.readableId
            this["retainedSeverities"] = retainedSeverities.map { it.readableId }
        }
    }

    companion object {
        fun map(json: JsonObject): Either<String, AnalysisFindingRetentionCriteria> {
            val type = FindingType.mapOptionalReadableId(json.string("type")) ?: return "Missing valid type property in JSON: ${json.toJsonString()}".left()
            val groupingCriteria = AnalysisFindingGroupingCriteria.mapOptionalReadableId(json.string("groupingCriteria")) ?: return "Missing valid groupingCriteria property in JSON: ${json.toJsonString()}".left()
            val severityIds = json.toStringListOrNull("retainedSeverities") ?: return "Missing retainedSeverities property in JSON: ${json.toJsonString()}".left()
            val retainedSeverities = severityIds.map { FindingSeverity.mapOptionalReadableId(it) ?: return "Invalid retainedSeverities id: $it".left() }.distinct().sorted()

            return AnalysisFindingRetentionCriteria(type, groupingCriteria, retainedSeverities).right()
        }
    }
}