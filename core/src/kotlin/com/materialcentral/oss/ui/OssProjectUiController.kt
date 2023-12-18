package com.materialcentral.oss.ui

import org.geezer.io.ui.UiController
import org.geezer.io.ui.findOr404
import com.materialcentral.oss.OssProjectsTable
import jakarta.servlet.http.HttpServletRequest

object OssProjectUiController : UiController() {
    override val jspPath: String = "oss/projects"


    fun getProject(id: Long, request: HttpServletRequest): String {
        val project = id.findOr404(OssProjectsTable)

        return viewJsp
    }
}