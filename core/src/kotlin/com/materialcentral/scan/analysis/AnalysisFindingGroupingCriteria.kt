package com.materialcentral.scan.analysis

import org.geezer.HasDescription
import org.geezer.db.ReadableDataEnum
import org.geezer.db.ReadableDataEnumType
import org.geezer.io.ui.FontIcon
import org.geezer.system.runtime.DataEnumProperty

enum class AnalysisFindingGroupingCriteria(override val id: Int, override val readableId: String, override val label: String, val icon: FontIcon, val minimumAnalyzersRequired: Int, override val description: String) : ReadableDataEnum, HasDescription {
    UNION(0, "union", "Union", FontIcon("fa-union", "f6a2"), 1, "All findings from all analyzers will be retained."),
    INTERSECTION(1, "intersection", "Intersection", FontIcon("fa-intersection", "f668"), 2, "Only findings found in all analyzers will be retained."),
    MAJORITY(2, "majority", "Majority", FontIcon("fa-intersection", "f668"), 3, "Only findings that were found by a majority of the used analyzers."),
    ;

    companion object : ReadableDataEnumType<AnalysisFindingGroupingCriteria> {
        val defaultProperty = DataEnumProperty("AnalysisFindingGroupingCriteriaDefault", AnalysisFindingGroupingCriteria) { UNION }

        override val enumValues = values()

        override val defaultValue: AnalysisFindingGroupingCriteria
            get() = defaultProperty()
    }
}