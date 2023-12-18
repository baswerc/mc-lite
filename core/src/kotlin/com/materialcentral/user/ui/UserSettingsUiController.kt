package com.materialcentral.user.ui

import arrow.core.toNonEmptyListOrNull
import com.materialcentral.user.UserRole
import com.materialcentral.user.UserSettings
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.geezer.io.hasWarnings
import org.geezer.io.ui.UiController
import org.geezer.io.ui.pageObject
import org.geezer.io.ui.redirect
import org.geezer.io.warn
import org.geezer.routes.RequestParameters

object UserSettingsUiController : UiController() {
    override val jspPath: String = "${UsersUiController.jspPath}/settings"

    fun getSettings(request: HttpServletRequest): String {
        request.pageObject = UserSettings.get()
        return indexJsp
    }

    fun postUpdate(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val defaultRoles = parameters.getValues("defaultRoles").mapNotNull { UserRole.mapIfValid(it.toIntOrNull()) }
        val settings = UserSettings.get()
        if (defaultRoles.isNullOrEmpty()) {
            request.warn("At least one default role is required.")
        } else {
            settings.defaultRoles = defaultRoles.toNonEmptyListOrNull()!!
        }

        if (!request.hasWarnings()) {
            UserSettings.save(settings)
        }

        return ::getSettings.redirect(request, response)
    }
}