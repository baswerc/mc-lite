package com.materialcentral.scan.analysis

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import org.geezer.json.Jsonable
import com.materialcentral.scan.FindingType

class AnalysisConfiguration(
    val analyzerId: String,
    var findingTypes: List<FindingType>,
    var analyzerConfiguration: JsonObject? = null,
) : Jsonable {

    override fun toJson(): JsonObject {
        return JsonObject().apply {
            this["analyzerId"] = analyzerId
            this["findingTypes"] = findingTypes.map { it.readableId }
            this["analyzerConfiguration"] = analyzerConfiguration
        }
    }

    companion object {
        fun map(json: JsonObject): Either<String, AnalysisConfiguration> {
            val analyzerId = json.string("analyzerId")
            if (analyzerId.isNullOrEmpty()) {
                return "Missing analyzerId property.".left()
            }

            val findingTypes = json.array<String>("findingTypes")?.mapNotNull { FindingType.mapOptionalReadableId(it) }?.toSet()
            if (findingTypes.isNullOrEmpty()) {
                return "Missing findingTypes property.".left()
            }

            val analyzerConfiguration = json.obj("analyzerConfiguration")

            return AnalysisConfiguration(analyzerId, findingTypes.distinct().sorted(), analyzerConfiguration).right()
        }
    }
}