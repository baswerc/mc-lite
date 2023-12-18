package com.materialcentral.user.authentication

import com.materialcentral.io.ui.HomeRoutes
import com.materialcentral.io.ui.optionalUserSession
import org.geezer.io.ui.RedirectMessagesFilter
import com.materialcentral.user.UserSession
import com.materialcentral.user.UserSessionsCache
import com.materialcentral.user.UserSessionsTable
import com.materialcentral.user.authentication.internal.InternalLoginUiRoutes
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.codec.binary.Base64
import org.geezer.routes.RequestParameters
import org.geezer.routes.redirectTo
import org.geezer.user.authentication.AppLoginUiController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KFunction

object LoginUiRoutes : AppLoginUiController() {

    fun getLogin(request: HttpServletRequest): String {
        return if (request.optionalUserSession != null) {
            HomeRoutes::get.redirectTo(request)
        } else {
            "${InternalLoginUiRoutes.JspPath}/login.jsp"
        }
    }

    fun getLogout(userSession: UserSession?): KFunction<*> {
        if (userSession != null) {
            UserSessionsTable.deactivate(userSession)
            UserSessionsCache.evictFromCache(userSession.identifier)
        }

        return HomeRoutes::get
    }




    companion object {
        const val JspPath = "/login"

        protected val log: Logger = LoggerFactory.getLogger(javaClass)

        fun redirectToOriginalRequest(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse, defaultRoute: KFunction<*> = HomeRoutes::get): String {
            val redirectUrl = parameters[InternalLoginUiRoutes.RedirectParameter]?.let {
                try {
                    Base64.decodeBase64(it).decodeToString()
                } catch (e: Exception) {
                    log.info("Unable to decode login URL redirect parameter.", e)
                    null
                }
            }

            return if (redirectUrl.isNullOrBlank()) {
                RedirectMessagesFilter.addMessages(request, response)
                defaultRoute.redirectTo(request)
            } else {
                "redirect:$redirectUrl"
            }

        }
    }
}