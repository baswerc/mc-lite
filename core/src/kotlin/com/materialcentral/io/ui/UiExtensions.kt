package com.materialcentral.io.ui

import com.materialcentral.tag.Tag
import com.materialcentral.user.User
import com.materialcentral.user.session.UserSession
import jakarta.servlet.http.HttpServletRequest
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

var HttpServletRequest.tags:List<Tag>
    get() = getAttribute("tags") as List<Tag>? ?: listOf()
    set(value) { setAttribute("tags", value) }


