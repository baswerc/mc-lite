package com.materialcentral.oss.ui

import org.geezer.io.ui.UiController
import jakarta.servlet.http.HttpServletRequest

object OssPackageUiController : UiController() {
    override val jspPath: String = "oss/packages"

    fun getPackage(id: Long, request: HttpServletRequest): String {
        TODO()
    }

    fun getRelease(id: Long, request: HttpServletRequest): String {
        TODO()
    }
}