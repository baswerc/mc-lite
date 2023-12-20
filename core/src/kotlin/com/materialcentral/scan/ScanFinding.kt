package com.materialcentral.scan

import com.materialcentral.DataStringsTable
import org.geezer.db.Data
import com.materialcentral.scan.analysis.AnalysisFinding
import com.materialcentral.scan.filter.ScanFindingFilter

abstract class ScanFinding(
    val scanId: Long,
    val analyzerIds: List<String>,
    val analyzerSeverities: Map<String, FindingSeverity>,
    var scanFindingFilterId: Long?,
) : Data() {

    abstract val type: FindingType

    abstract val locationId: Long?

    open val filterFindingTypes: Set<FindingType> = setOf(type)

    protected abstract fun detailsMatchFilter(filter: ScanFindingFilter): Boolean

    open fun lookupSeverity(): FindingSeverity? {
        return null
    }

    fun matchesFilter(filter: ScanFindingFilter): Boolean {
        if (!filterFindingTypes.contains(filter.findingType)) {
            return false
        }

        if (filter.locationRegexs.isNotEmpty()) {
            val location = DataStringsTable.findById(locationId)
            if (location == null || filter.locationRegexs.none { it.matches(location.value) }) {
                return false
            }
        }

        return detailsMatchFilter(filter)
    }

    companion object {
        fun getIdsAndSeverities(analysisFindings: List<AnalysisFinding>): Pair<List<String>, Map<String, FindingSeverity>> {
            val ids = analysisFindings.map { it.analyzerId }.sorted()
            val severities = analysisFindings.mapNotNull { analysisFinding -> analysisFinding.analyzerSeverity?.let { analysisFinding.analyzerId to it } }.associate { it.first to it.second }
            return ids to severities
        }
    }
}