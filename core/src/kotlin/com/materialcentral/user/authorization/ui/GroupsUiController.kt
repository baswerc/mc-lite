package com.materialcentral.user.authorization.ui

import com.materialcentral.user.UserRole
import com.materialcentral.user.authorization.Group
import com.materialcentral.user.authorization.GroupsTable
import com.materialcentral.user.ui.UsersUiController
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.geezer.db.schema.eqIgnoreCase
import org.geezer.io.hasWarnings
import org.geezer.io.ui.UiController
import org.geezer.io.ui.findOr404
import org.geezer.io.ui.pageObject
import org.geezer.io.ui.redirect
import org.geezer.io.ui.table.isUiTableRequest
import org.geezer.io.warn
import org.geezer.routes.RequestParameters
import org.geezer.routes.TerminateRouteException
import org.geezer.routes.redirect
import org.geezer.system.runtime.RuntimeClock

object GroupsUiController : UiController() {
    override val jspPath: String = "${UsersUiController.jspPath}/groups"

    fun getAll(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        if (request.isUiTableRequest) {
            GroupsUiTable.toHTML(request, parameters, response)
            throw TerminateRouteException()
        }

        return indexJsp
    }

    fun getGroup(id: Long, request: HttpServletRequest): String {
        val group = id.findOr404(GroupsTable)
        request.pageObject = group
        return viewJsp
    }

    fun getEditGroup(id: Long, request: HttpServletRequest): String {
        val group = id.findOr404(GroupsTable)
        request.pageObject = group
        return editJsp
    }

    fun postUpdateGroup(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val group = id.findOr404(GroupsTable)
        updateGroup(group, request, parameters)

        return if (request.hasWarnings()) {
            getEditGroup(id, request)
        } else {
            GroupsTable.update(group)
            ::getAll.redirect(request, response)
        }
    }

    fun getNewGroup(request: HttpServletRequest): String {
        request.pageObject = Group("", null, listOf(), true, RuntimeClock.transactionAt)
        return newJsp
    }

    fun postCreateGroup(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val group = Group("", null, listOf(), true, RuntimeClock.transactionAt)
        updateGroup(group, request, parameters)

        return if (request.hasWarnings()) {
            request.pageObject = group
            newJsp
        } else {
            GroupsTable.create(group)
            ::getAll.redirect(request, response)
        }
    }

    fun updateGroup(group: Group, request: HttpServletRequest, parameters: RequestParameters) {
        val name = parameters["name"]
        if (name.isNullOrBlank()) {
            request.warn("Group name is required.")
        } else if (!name.equals(group.name, true) && GroupsTable.rowsExists { (GroupsTable.name eqIgnoreCase name) }) {
            request.warn("A group name with the provided name already exists.")
        } else {
            group.name = name
        }

        group.memberRoles = parameters.getInts("memberRoles").mapNotNull { UserRole.mapIfValid(it) }
        group.description = parameters["description"]
        group.active = parameters.getBoolean("active", group.active)
    }
}