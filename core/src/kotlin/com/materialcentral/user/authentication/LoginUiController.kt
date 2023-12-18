package com.materialcentral.user.authentication

import com.materialcentral.io.ui.HomeUiController
import com.materialcentral.io.ui.optionalUserSession
import com.materialcentral.user.session.UserSessionRequestManager
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.geezer.routes.RequestParameters
import org.geezer.user.authentication.AppLoginUiController
import kotlin.reflect.KFunction

object LoginUiController : AppLoginUiController() {
    override val defaultRoute: KFunction<*> = HomeUiController::get

    override fun hasValidSession(request: HttpServletRequest): Boolean {
        return request.optionalUserSession != null
    }

    override fun deleteUserSession(request: HttpServletRequest, response: HttpServletResponse) {
        UserSessionRequestManager.deleteSession(request, response)
    }

    fun postLogin(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse) {

    }
}