package com.materialcentral.scan.filter.ui

import com.materialcentral.scan.ui.ScansUiController
import jakarta.servlet.http.HttpServletRequest
import org.geezer.io.ui.UiController

object ScanFindingFiltersUiController : UiController() {
    override val jspPath: String = "${ScansUiController.jspPath}/filters"

    fun getFilter(id: Long, request: HttpServletRequest): String {
        return viewJsp
    }
}