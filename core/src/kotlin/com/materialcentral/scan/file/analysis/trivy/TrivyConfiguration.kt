package com.materialcentral.scan.file.analysis.trivy

import com.beust.klaxon.JsonObject
import org.geezer.json.Jsonable
import org.geezer.toStringList

class TrivyConfiguration(
    val ignoredVulnerabilityStatuses: List<TrivyVulnerabilityStatus>,
    val vulnerabilityTarget: TrivyVulnerabilityTarget?

) : Jsonable {

    constructor() : this(DefaultIgnoredVulnerabilityStatuses, DefaultVulnerabilityTarget)

    override fun toJson(): JsonObject {
        return JsonObject().apply {
            this["ignoredVulnerabilityStatuses"] = ignoredVulnerabilityStatuses.map { it.readableId }
            this["vulnerabilityTarget"] = vulnerabilityTarget?.readableId
        }
    }

    companion object {
        const val DefaultIgnoreUnfixedVulnerabilities = false

        val DefaultIgnoredVulnerabilityStatuses: List<TrivyVulnerabilityStatus> = listOf(TrivyVulnerabilityStatus.AFFECTED, TrivyVulnerabilityStatus.WILL_NOT_FIX, TrivyVulnerabilityStatus.FIX_DEFERRED, TrivyVulnerabilityStatus.END_OF_LIFE)

        val DefaultVulnerabilityTarget: TrivyVulnerabilityTarget? = null

        @JvmStatic
        fun map(json: JsonObject?): TrivyConfiguration {
            if (json == null) {
                return TrivyConfiguration()
            }

            val ignoredVulnerabilityStatuses = json.toStringList("ignoredVulnerabilityStatuses").mapNotNull { TrivyVulnerabilityStatus.mapOptionalReadableId(it) }
            val vulnerabilityTarget = TrivyVulnerabilityTarget.mapOptionalReadableId(json.string("vulnerabilityTarget"))

            return TrivyConfiguration(ignoredVulnerabilityStatuses, vulnerabilityTarget)
        }
    }
}