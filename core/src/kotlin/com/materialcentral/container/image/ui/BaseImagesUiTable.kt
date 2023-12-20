package com.materialcentral.container.image.ui

import com.materialcentral.container.image.ContainerImagesTable
import com.materialcentral.os.Architecture
import com.materialcentral.os.OperatingSystemType
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.span
import org.geezer.db.schema.ilike
import org.geezer.db.schema.optionalEnumLike
import org.geezer.io.ui.table.UiColumn
import org.geezer.io.ui.table.UiTable
import org.geezer.routes.urls.UrlGen
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.or

abstract class BaseImagesUiTable() : UiTable(rowDetailsColumn = ContainerImagesTable.id, rowDetailsRoute = ContainerImagesUiController::getImageRowDetails) {

    override val defaultSort: List<Pair<Column<*>, SortOrder>> = listOf(ContainerImagesTable.createdAt to SortOrder.DESC)

    override fun initializeColumns(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Name", tableColumn = ContainerImagesTable.name) { flowContent, row, request, _ ->
            flowContent.a(href = UrlGen.url(ContainerImagesUiController::getImage, row[ContainerImagesTable.id], request)) { +row[ContainerImagesTable.name] }
        })

        columns.add(UiColumn("Architecture", tableColumn = ContainerImagesTable.architecture) { flowContent, row, _, _ ->
            flowContent.span {
                +(row[ContainerImagesTable.architecture]?.label ?: "")
            }
        })


        columns.add(UiColumn("Created At", tableColumn = ContainerImagesTable.createdAt) { flowContent, row, _, _ ->
            flowContent.div("timestamp") {
                text(row[ContainerImagesTable.createdAt])
            }
        })
    }

    fun createSearchQueryFilter(searchQuery: String): Op<Boolean> {
        return  (ContainerImagesTable.name ilike searchQuery) or (ContainerImagesTable.digest ilike searchQuery) or
                (ContainerImagesTable.architecture.optionalEnumLike(searchQuery, Architecture)) or (ContainerImagesTable.os.optionalEnumLike(searchQuery, OperatingSystemType))
    }
}