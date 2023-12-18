package com.materialcentral.user.ui

import com.materialcentral.user.User
import com.materialcentral.user.UsersTable
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.span
import org.geezer.db.schema.ilike
import org.geezer.io.ui.table.UiColumn
import org.geezer.routes.RequestParameters
import org.geezer.user.ui.AppUsersUiTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

object UsersUiTable : AppUsersUiTable(UsersTable) {
    override fun initializeColumns(columns: MutableList<UiColumn>) {
        columns.add(nameColumn)
        columns.add(emailColumn)
        columns.add(createdAtColumn)
        columns.add(lastLoginAtColumn)

        /*
        columns.add(UiColumn("Username", UsersTable.username, icon = User.ExternalIdentifierIcon) { html, row, _, _ ->
            UsersTable.username?.let { html.span() { +(row[])} }
        })
         */
    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        return if (searchQuery.isNullOrBlank()) {
            UsersTable.selectAll()
        } else {
            UsersTable.select { (UsersTable.name ilike searchQuery) or (UsersTable.email ilike searchQuery) or (UsersTable.username ilike searchQuery) }
        }
    }
}