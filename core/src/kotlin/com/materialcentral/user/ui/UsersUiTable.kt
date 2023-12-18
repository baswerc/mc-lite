package com.materialcentral.user.ui

import com.materialcentral.user.User
import com.materialcentral.user.UsersTable
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.span
import org.geezer.io.ui.table.UiColumn
import org.geezer.routes.RequestParameters
import org.geezer.user.ui.AppUsersUiTable
import org.jetbrains.exposed.sql.Query

object UsersUiTable : AppUsersUiTable(UsersTable) {
    override fun initializeColumns(columns: MutableList<UiColumn>) {
        columns.add(nameColumn)
        columns.add(UiColumn("Identifier", UsersTable.externalIdentifier, icon = User.ExternalIdentifierIcon) { html, row, _, _ ->
            UsersTable.externalIdentifier?.let { html.span() { +(row[])} }
        })
    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        TODO("Not yet implemented")
    }
}