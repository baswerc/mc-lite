package com.materialcentral.user.authorization.ui

import com.materialcentral.user.authorization.GroupsTable
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.span
import org.geezer.db.schema.ilike
import org.geezer.io.ui.addTimestampTo
import org.geezer.io.ui.addTo
import org.geezer.io.ui.table.UiColumn
import org.geezer.io.ui.table.UiTable
import org.geezer.routes.RequestParameters
import org.jetbrains.exposed.sql.*

object GroupsUiTable : UiTable(GroupsTable.id) {
    override val defaultSort: List<Pair<Column<*>, SortOrder>> = listOf(GroupsTable.name to SortOrder.ASC)

    override fun initializeColumns(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Name", GroupsTable.name, description = "The name of the group.") { html, row, _, _ ->
            html.span { +(row[GroupsTable.name]) }
        })

        columns.add(UiColumn("Member Roles", GroupsTable.name) { html, row, _, _ ->
            html.span { +(row[GroupsTable.memberRoles].joinToString(", ") { it.label }) }
        })

        columns.add(UiColumn("Active", GroupsTable.name) { html, row, _, _ ->
            row[GroupsTable.active].addTo(html)
        })

        columns.add(UiColumn("Created At", GroupsTable.name) { html, row, _, _ ->
            row[GroupsTable.createdAt].addTimestampTo(html)
        })
    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        return if (searchQuery.isNullOrBlank()) {
            GroupsTable.selectAll()
        } else {
            GroupsTable.select { (GroupsTable.name ilike searchQuery) }
        }
    }
}