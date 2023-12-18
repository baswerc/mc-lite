package com.materialcentral.container.image.ui

import org.geezer.db.schema.ilike
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.table.UiColumn
import org.geezer.io.ui.table.UiTable
import com.materialcentral.container.image.ContainerImageLayersTable
import com.materialcentral.container.image.ContainerImageTag
import com.materialcentral.container.image.ContainerImageTagsTable
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.div
import org.geezer.routes.RequestParameters
import org.jetbrains.exposed.sql.*

class ImageTagsUiTable(val containerImageId: Long) : UiTable() {

    override val defaultSort: List<Pair<Column<*>, SortOrder>> = listOf(ContainerImageTagsTable.removedAt to SortOrder.ASC_NULLS_FIRST, ContainerImageTagsTable.addedAt to SortOrder.ASC)

    override fun initializeColumns(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Tag", icon = ContainerImageTag.Icon, tableColumn = ContainerImageTagsTable.value) { flowContent, row, request, _ ->
            flowContent.text(row[ContainerImageTagsTable.value])
        })

        columns.add(UiColumn("Added At", icon = FontIcon.CalendarAdded, tableColumn = ContainerImageLayersTable.bytesSize) { flowContent, row, request, _ ->
            flowContent.div("timestamp") { text(row[ContainerImageTagsTable.addedAt])}
        })

        columns.add(UiColumn("Removed At", icon = FontIcon.CalendarRemoved, tableColumn = ContainerImageLayersTable.index) { flowContent, row, request, _ ->
            flowContent.div("timestamp") { text(row[ContainerImageTagsTable.removedAt]?.toString() ?: "")}
        })
    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        var query = ContainerImageTagsTable.select { ContainerImageTagsTable.containerImageId eq containerImageId }

        if (!searchQuery.isNullOrBlank()) {
            query = query.andWhere { ContainerImageTagsTable.value ilike searchQuery }
        }

        return query
    }
}