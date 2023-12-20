package com.materialcentral.scan.schedule.ui

import com.materialcentral.scan.schedule.ScanSchedulesTable
import com.materialcentral.scan.ui.ScansUiController
import com.materialcentral.user.authorization.Role
import jakarta.servlet.http.HttpServletRequest
import org.geezer.io.ui.UiController
import com.materialcentral.io.ui.findOr404Or403
import org.geezer.io.ui.pageObject

object ScanScheduleUiController : UiController() {
    override val jspPath: String = "${ScansUiController.jspPath}/schedules"

    fun get(id: Long, request: HttpServletRequest): String {
        val schedule = id.findOr404Or403(ScanSchedulesTable, request, Role.VIEWER)
        request.pageObject = schedule
        return viewJsp
    }
}