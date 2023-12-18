package com.materialcentral.user.session

import com.materialcentral.io.ui.userSession
import com.materialcentral.user.authentication.UserAuthenticationProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.geezer.routes.IgnoreAssetsApiFilter
import kotlin.reflect.KFunction

class UserSessionFilter : IgnoreAssetsApiFilter() {
    override fun processRequest(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain, route: KFunction<*>?) {
        var session = UserSessionRequestManager.getSessionFrom(request)
        if (session == null) {
            session = UserAuthenticationProvider.createUserSessionFromRequest(request, response)
        }

        if (session != null) {
            request.userSession = session
        }

        chain.doFilter(request, response)
    }
}