package com.materialcentral.scan.ui

import com.materialcentral.DataStringsTable
import com.materialcentral.scan.ScanTargetType
import com.materialcentral.scan.ScansTable
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.a
import org.geezer.db.schema.and
import org.geezer.db.schema.enumLike
import org.geezer.db.schema.ilike
import org.geezer.db.schema.or
import org.geezer.io.ui.UI
import org.geezer.io.ui.format
import org.geezer.io.ui.table.UiColumn
import org.geezer.routes.RequestParameters
import org.geezer.routes.urls.UrlGen
import org.geezer.task.ui.TasksUiTable
import org.jetbrains.exposed.sql.*

object ScansUiTable : TasksUiTable(ScansTable) {
    override val defaultSort: List<Pair<Column<*>, SortOrder>> = listOf()

    override fun initializeColumns(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Target", DataStringsTable.value) { html, row, request, _ ->
            val type = row[ScansTable.scanTargetType]
            val id = row[ScansTable.scanTargetId]
            val name = row[DataStringsTable.value]

            val url = UrlGen.url(type.route, id, request)
            html.a(url) {
                type.icon.addTo(html)
                +(" $name")
            }
        })

        columns.add(createStateColumn())
        columns.add(createCreatedAtColumn())

        columns.add(UiColumn("Findings", listOf(ScansTable.criticalFindings, ScansTable.highFindings, ScansTable.mediumFindings, ScansTable.lowFindings)) { html, row, _, _ ->
            val critical = row[ScansTable.criticalFindings]
            val high = row[ScansTable.highFindings]
            val medium = row[ScansTable.mediumFindings]
            val low = row[ScansTable.lowFindings]
            html.text("${critical.format()}C ${high.format()}H ${medium.format()}M ${low.format()}L")
        })
    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        val andCriteria = mutableListOf<Op<Boolean>>()
        val orCriteria = mutableListOf<Op<Boolean>>()

        addCriteria(request, parameters, searchQuery, orCriteria, andCriteria)

        if (!searchQuery.isNullOrBlank()) {
            orCriteria.add(DataStringsTable.value ilike searchQuery)
            orCriteria.add(ScansTable.scanTargetType.enumLike(searchQuery, ScanTargetType))
        }

        var where = andCriteria.and() and orCriteria.or()

        return ScansTable.innerJoin(DataStringsTable, { scanTargetNameId }, { id }).select(where)
    }
}