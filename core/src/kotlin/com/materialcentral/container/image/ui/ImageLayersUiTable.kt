package com.materialcentral.container.image.ui

import org.geezer.db.schema.ilike
import org.geezer.io.ui.UI
import org.geezer.io.ui.table.UiColumn
import org.geezer.io.ui.table.UiTable
import com.materialcentral.container.image.ContainerImageLayersTable
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.span
import org.geezer.routes.RequestParameters
import org.jetbrains.exposed.sql.*

class ImageLayersUiTable(val containerImageId: Long) : UiTable(null) {

    override val defaultSort: List<Pair<Column<*>, SortOrder>> = listOf(ContainerImageLayersTable.index to SortOrder.ASC)

    override fun initializeColumns(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Index", tableColumn = ContainerImageLayersTable.index, noBreak = true) { flowContent, row, request, _ ->
            flowContent.text(row[ContainerImageLayersTable.index])
        })

        columns.add(UiColumn("Size", tableColumn = ContainerImageLayersTable.bytesSize, noBreak = true) { flowContent, row, request, _ ->
            flowContent.text(UI.formatBytes(row[ContainerImageLayersTable.bytesSize]))
        })

        columns.add(UiColumn("Parent", tableColumn = ContainerImageLayersTable.index, noBreak = true) { flowContent, row, request, _ ->
            flowContent.text(UI.formatBoolean(row[ContainerImageLayersTable.parentLayer]))
        })

        columns.add(UiColumn("Digest", tableColumn = ContainerImageLayersTable.index) { flowContent, row, request, _ ->
            flowContent.span {
                attributes["style"] = "word-break: break-word;"
                +(row[ContainerImageLayersTable.digest])
            }
        })

    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        var query = ContainerImageLayersTable.select { ContainerImageLayersTable.containerImageId eq containerImageId }

        if (!searchQuery.isNullOrBlank()) {
            query = query.andWhere { ContainerImageLayersTable.digest ilike searchQuery }
        }

        return query
    }
}