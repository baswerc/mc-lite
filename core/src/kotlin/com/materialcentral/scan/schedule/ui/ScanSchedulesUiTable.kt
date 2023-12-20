package com.materialcentral.scan.schedule.ui

import com.materialcentral.RootPathsTable
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.UI
import org.geezer.io.ui.table.UiColumn
import org.geezer.io.ui.table.UiTable
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.ScanTarget
import com.materialcentral.scan.ScanTargetType
import com.materialcentral.scan.schedule.ScanSchedule
import com.materialcentral.scan.schedule.ScanSchedulesTable
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.a
import kotlinx.html.i
import kotlinx.html.span
import org.geezer.routes.RequestParameters
import org.geezer.routes.urls.UrlGen
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class ScanSchedulesUiTable(val target: ScanTarget, val includeInactive: Boolean, val includeScanTarget: Boolean) : UiTable(rowDetailsColumn = ScanSchedulesTable.id) {
    override val defaultSort: List<Pair<Column<*>, SortOrder>> = listOf(ScanSchedulesTable.name to SortOrder.ASC_NULLS_LAST)

    override fun initializeColumns(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Name", icon = ScanSchedule.Icon, tableColumn = ScanSchedulesTable.name) { flowContent, row, request, _ ->
            flowContent.a(href = UrlGen.url(ScanScheduleUiController::get, row[ScanSchedulesTable.id], request)) { +row[ScanSchedulesTable.name] }
        })

        if (includeInactive) {
            columns.add(UiColumn("Active", icon = FontIcon.Active, tableColumn = ScanSchedulesTable.active) { flowContent, row, request, _ ->
                flowContent.span {
                    +(UI.formatBoolean(row[ScanSchedulesTable.active]))
                }
            })
        }

        if (includeScanTarget) {
            columns.add(UiColumn("Target Type", icon = ScanTargetType.Icon, tableColumn = ScanSchedulesTable.targetType) { flowContent, row, request, _ ->
                val type = row[ScanSchedulesTable.targetType]
                flowContent.span {
                    i(type.icon.getCssClass())
                    +(" ${type.label}")
                }
            })
        }

        columns.add(UiColumn("Scan Medium", icon = ScanMedium.Icon, tableColumn = ScanSchedulesTable.medium) { flowContent, row, request, _ ->
            val type = row[ScanSchedulesTable.medium]
            flowContent.span {
                i(type.icon.getCssClass())
                +(" ${type.label}")
            }
        })
    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        val pathToRootIds = RootPathsTable.findPathToRoot(target.groupId, target.groupType, true).map { it.first }
        val query = if (pathToRootIds.isEmpty()) {
            Op.FALSE
        } else {
            var query = (ScanSchedulesTable.targetType eq target.scanTargetType) and (ScanSchedulesTable.parentId inList pathToRootIds)
            if (!includeInactive) {
                query = query and (ScanSchedulesTable.active eq true)
            }
            query
        }

        return ScanSchedulesTable.select(query)
    }
}