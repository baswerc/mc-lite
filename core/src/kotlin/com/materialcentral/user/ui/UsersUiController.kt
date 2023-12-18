package com.materialcentral.user.ui

import org.geezer.io.ui.findOr404
import org.geezer.io.ui.pageObject
import jakarta.servlet.http.HttpServletRequest
import org.geezer.io.ui.UiController
import org.geezer.io.ui.table.isUiTableRequest
import org.geezer.routes.TerminateRouteException

object UsersUiController : UiController() {
    override val jspPath: String = "/users"

    fun getAll(request: HttpServletRequest): String {
        if (request.isUiTableRequest) {
            throw TerminateRouteException()
        }

        return indexJsp
    }

    fun get(id: Long, request: HttpServletRequest): String {
        val user = id.findOr404(UsersTable)
        request.pageObject = user
        return "$JspPath/view.jsp"
    }

}