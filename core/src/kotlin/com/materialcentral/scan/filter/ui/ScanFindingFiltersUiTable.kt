package com.materialcentral.scan.filter.ui

import com.materialcentral.scan.FindingType
import com.materialcentral.CandidateMatchType
import com.materialcentral.scan.filter.ScanFindingFilterReason
import com.materialcentral.scan.filter.ScanFindingFiltersTable
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.span
import org.geezer.db.schema.enumLike
import org.geezer.db.schema.ilike
import org.geezer.io.ui.addTimestampTo
import org.geezer.io.ui.addTo
import org.geezer.io.ui.table.UiColumn
import org.geezer.io.ui.table.UiTable
import org.geezer.routes.RequestParameters
import org.jetbrains.exposed.sql.*

object ScanFindingFiltersUiTable : UiTable(ScanFindingFiltersTable.id) {
    override val defaultSort: List<Pair<Column<*>, SortOrder>> = listOf(ScanFindingFiltersTable.name to SortOrder.ASC)

    override fun initializeColumns(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Name") { html, row, _, _ ->
            html.span { +row[ScanFindingFiltersTable.name] }
        })

        columns.add(UiColumn("Finding Type") { html, row, _, _ ->
            row[ScanFindingFiltersTable.findingType].addTo(html)
        })

        columns.add(UiColumn("Candidate Type") { html, row, _, _ ->
            row[ScanFindingFiltersTable.candidateType].addTo(html)
        })

        columns.add(UiColumn("Reason") { html, row, _, _ ->
            row[ScanFindingFiltersTable.reason].addTo(html)
        })

        columns.add(UiColumn("Active") { html, row, _, _ ->
            row[ScanFindingFiltersTable.active].addTo(html)
        })

        columns.add(UiColumn("Created At") { html, row, _, _ ->
            row[ScanFindingFiltersTable.createdAt].addTimestampTo(html)
        })

    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        return if (searchQuery.isNullOrBlank()) {
            ScanFindingFiltersTable.selectAll()
        } else {
            ScanFindingFiltersTable.select {
                (ScanFindingFiltersTable.name ilike searchQuery) or
                (ScanFindingFiltersTable.description ilike searchQuery) or
                (ScanFindingFiltersTable.candidateType.enumLike(searchQuery, CandidateMatchType)) or
                (ScanFindingFiltersTable.findingType.enumLike(searchQuery, FindingType)) or
                (ScanFindingFiltersTable.reason.enumLike(searchQuery, ScanFindingFilterReason))
            }
        }
    }
}