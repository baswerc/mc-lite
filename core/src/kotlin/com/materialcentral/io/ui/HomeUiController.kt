package com.materialcentral.io.ui

import jakarta.servlet.http.HttpServletRequest
import org.geezer.io.ui.UiController

object HomeUiController : UiController() {
    override val jspPath: String = "/"

    fun get(request: HttpServletRequest): String {
        return indexJsp
    }
}