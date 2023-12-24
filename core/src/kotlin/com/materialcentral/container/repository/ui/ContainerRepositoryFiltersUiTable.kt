package com.materialcentral.container.repository.ui

import com.materialcentral.CandidateMatchType
import com.materialcentral.container.repository.ContainerRepository
import com.materialcentral.container.repository.ContainerRepositoryTagsTable
import com.materialcentral.scan.filter.ScanFindingFilter
import com.materialcentral.scan.filter.ScanFindingFilterOwnersTable
import com.materialcentral.scan.filter.ScanFindingFilterTagsTable
import com.materialcentral.scan.filter.ScanFindingFiltersTable
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.a
import org.geezer.db.schema.ilike
import org.geezer.db.schema.isTrue
import org.geezer.io.ui.addTo
import org.geezer.io.ui.table.UiColumn
import org.geezer.io.ui.table.UiTable
import org.geezer.routes.RequestParameters
import org.geezer.routes.urls.UrlGen
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ContainerRepositoryFiltersUiTable(val containerRepository: ContainerRepository) : UiTable(ScanFindingFiltersTable.id) {

    override val defaultSort: List<Pair<Column<*>, SortOrder>> = listOf(ScanFindingFiltersTable.name to SortOrder.ASC)

    override fun initializeColumns(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Name", ScanFindingFiltersTable.name) {html, row, request, _ ->
            val url = UrlGen.url(ScanFindingFilter.Route, row[ScanFindingFiltersTable.id], request)
            html.a(url) { +(row[ScanFindingFiltersTable.name]) }
        })

        columns.add(UiColumn("Active", ScanFindingFiltersTable.active) {html, row, _, _ ->
            row[ScanFindingFiltersTable.active].addTo(html)
        })

        columns.add(UiColumn("Type", ScanFindingFiltersTable.findingType) {html, row, _, _ ->
            row[ScanFindingFiltersTable.findingType].addTo(html)
        })

        columns.add(UiColumn("Reason", ScanFindingFiltersTable.reason) {html, row, _, _ ->
            row[ScanFindingFiltersTable.reason].addTo(html)
        })

    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        var where: Op<Boolean> = ScanFindingFiltersTable.active.isTrue() and
                ((ScanFindingFiltersTable.candidateMatchType eq CandidateMatchType.ALL) or

                ((ScanFindingFiltersTable.candidateMatchType eq CandidateMatchType.MATCHED_TAGS) and
                    (exists(ScanFindingFilterTagsTable.select { (ScanFindingFilterTagsTable.scanFindingFilterId eq ScanFindingFiltersTable.id) and exists(
                        ContainerRepositoryTagsTable.select { (ContainerRepositoryTagsTable.tagId eq ScanFindingFilterTagsTable.tagId) and (ContainerRepositoryTagsTable.containerRepositoryId eq containerRepository.id) }
                    )}))) or

                ((ScanFindingFiltersTable.candidateMatchType eq CandidateMatchType.ASSIGNED) and exists(
                    ScanFindingFilterOwnersTable.select { (ScanFindingFilterOwnersTable.scanFindingFilterId eq ScanFindingFiltersTable.id) and (ScanFindingFilterOwnersTable.scanTargetSourceId eq containerRepository.id) })))

        if (!searchQuery.isNullOrBlank()) {
            where = where and ((ScanFindingFiltersTable.name ilike searchQuery) or (ScanFindingFiltersTable.description ilike searchQuery))
        }

        return ScanFindingFiltersTable.select(where)
    }
}