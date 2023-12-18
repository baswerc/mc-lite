package com.materialcentral.io.ui

import com.materialcentral.MaterialNode
import com.materialcentral.tag.Tag
import com.materialcentral.user.User
import com.materialcentral.user.UserSession
import com.materialcentral.user.authorization.Role
import jakarta.servlet.http.HttpServletRequest
import org.geezer.db.Data
import org.geezer.db.schema.DataTable
import org.geezer.io.ui.findOr404
import org.geezer.routes.ReturnStatus

val HttpServletRequest.isAuthenticated: Boolean
    get() = optionalUserSession != null

val HttpServletRequest.optionalUserSession: UserSession?
    get() = getAttribute("userSession") as UserSession?

var HttpServletRequest.userSession: UserSession
    get() = optionalUserSession ?: throw ReturnStatus.Unauthorized401
    set(value) {
        setAttribute("userSession", value)
    }

val HttpServletRequest.optionalUser: User?
    get() = optionalUserSession?.cachedUser

val HttpServletRequest.user: User
    get() = userSession.cachedUser

fun <T : Data> Long.findOr404Or403(table: DataTable<T>, request: HttpServletRequest, role: Role): T where T : MaterialNode {
    val value = findOr404(table)

    if (!request.userSession.hasRoleFor(value, role)) {
        throw ReturnStatus.Forbidden403
    }

    return value
}

var HttpServletRequest.tags:List<Tag>
    get() = getAttribute("tags") as List<Tag>? ?: listOf()
    set(value) { setAttribute("tags", value) }


