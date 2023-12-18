package com.materialcentral.user.ui

import com.materialcentral.user.UsersTable
import org.geezer.io.ui.findOr404
import org.geezer.io.ui.pageObject
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.geezer.io.ui.UiController
import org.geezer.io.ui.table.isUiTableRequest
import org.geezer.routes.RequestParameters
import org.geezer.routes.TerminateRouteException

object UsersUiController : UiController() {
    override val jspPath: String = "/users"

    fun getAll(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        if (request.isUiTableRequest) {
            UsersUiTable.toHTML(request, parameters, response)
            throw TerminateRouteException()
        }

        return indexJsp
    }

    fun get(id: Long, request: HttpServletRequest): String {
        val user = id.findOr404(UsersTable)
        request.pageObject = user
        return viewJsp
    }
}