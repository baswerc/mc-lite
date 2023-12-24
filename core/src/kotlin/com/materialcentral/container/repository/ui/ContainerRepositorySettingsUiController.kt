package com.materialcentral.container.repository.ui

import com.materialcentral.container.repository.ContainerRepositorySettings
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.geezer.io.ui.UiController
import org.geezer.io.ui.pageObject
import org.geezer.io.ui.redirect
import org.geezer.routes.RequestParameters

object ContainerRepositorySettingsUiController : UiController() {
    override val jspPath: String = "${ContainerRepositoryUiController.jspPath}/settings"

    fun getSettings(request: HttpServletRequest): String {
        request.pageObject = ContainerRepositorySettings.get()
        return indexJsp
    }

    fun getEditSettings(request: HttpServletRequest): String {
        request.pageObject = ContainerRepositorySettings.get()
        return editJsp
    }

    fun postUpdateSettings(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val settings = ContainerRepositorySettings.get()
        settings.minMinutesBetweenNewImagesCheck = parameters.getInt("minMinutesBetweenNewImagesCheck", settings.minMinutesBetweenNewImagesCheck).coerceAtLeast(1)
        settings.minMinutesBetweenFullSynchronization = parameters.getInt("minMinutesBetweenFullSynchronization", settings.minMinutesBetweenFullSynchronization).coerceAtLeast(settings.minMinutesBetweenNewImagesCheck)
        ContainerRepositorySettings.save(settings)
        return ::getSettings.redirect(request, response)
    }
}