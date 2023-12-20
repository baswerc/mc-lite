package com.materialcentral.container.repository.ui

import com.materialcentral.CandidateMatchType
import com.materialcentral.container.repository.ContainerRepositoriesTable
import com.materialcentral.container.repository.ContainerRepository
import com.materialcentral.container.repository.ContainerRepositoryTagsTable
import com.materialcentral.scan.ScanTargetSourceType
import com.materialcentral.scan.schedule.ScanScanScheduleTagsTable
import com.materialcentral.scan.schedule.ScanSchedule
import com.materialcentral.scan.schedule.ScanScheduleOwnersTable
import com.materialcentral.scan.schedule.ScanSchedulesTable
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.span
import org.geezer.db.schema.contains
import org.geezer.db.schema.ilike
import org.geezer.db.schema.isTrue
import org.geezer.io.ui.addTo
import org.geezer.io.ui.table.UiColumn
import org.geezer.io.ui.table.UiTable
import org.geezer.routes.RequestParameters
import org.geezer.routes.urls.UrlGen
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ContainerRepositoryScanSchedulesUiTable(val containerRepository: ContainerRepository) : UiTable(ScanSchedulesTable.id) {

    override val defaultSort: List<Pair<Column<*>, SortOrder>> = listOf(ScanSchedulesTable.name to SortOrder.ASC)

    override fun initializeColumns(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Name", ScanSchedulesTable.name) {html, row, request, _ ->
            val url = UrlGen.url(ScanSchedule.Route, row[ScanSchedulesTable.id], request)
            html.a(url) { +(row[ScanSchedulesTable.name]) }
        })

        columns.add(UiColumn("Active", ScanSchedulesTable.name) {html, row, _, _ ->
            row[ScanSchedulesTable.active].addTo(html)
        })

        columns.add(UiColumn("Scan Medium", ScanSchedulesTable.name) {html, row, _, _ ->
            row[ScanSchedulesTable.medium].addTo(html)
        })

    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        var where: Op<Boolean> = ScanSchedulesTable.active.isTrue() and
                (((ScanSchedulesTable.candidateMatchType eq CandidateMatchType.ALL) and (ScanSchedulesTable.scanTargetSourceTypes contains ScanTargetSourceType.CONTAINER_REPOSITORY)) or

                ((ScanSchedulesTable.candidateMatchType eq CandidateMatchType.MATCHED_TAGS) and (ScanSchedulesTable.scanTargetSourceTypes contains ScanTargetSourceType.CONTAINER_REPOSITORY) and
                    (exists(ScanScanScheduleTagsTable.select { (ScanScanScheduleTagsTable.scanScheduleId eq ScanSchedulesTable.id) and exists(
                        ContainerRepositoryTagsTable.select { (ContainerRepositoryTagsTable.tagId eq ScanScanScheduleTagsTable.tagId) and (ContainerRepositoryTagsTable.containerRepositoryId eq containerRepository.id) }
                    )}))) or

                ((ScanSchedulesTable.candidateMatchType eq CandidateMatchType.ASSIGNED) and exists(
                    ScanScheduleOwnersTable.select { (ScanScheduleOwnersTable.scanScheduleId eq ScanSchedulesTable.id) and (ScanScheduleOwnersTable.scanTargetSourceId eq containerRepository.id) })))

        if (!searchQuery.isNullOrBlank()) {
            where = where and ((ScanSchedulesTable.name ilike searchQuery) or (ScanSchedulesTable.description ilike searchQuery))
        }

        return ScanSchedulesTable.select(where)
    }
}