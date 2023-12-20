package com.materialcentral.scan.filter.ui

import com.materialcentral.scan.ui.ScansUiController
import org.geezer.io.ui.UiController

object ScanFindingFiltersUiController : UiController() {
    override val jspPath: String = "${ScansUiController.jspPath}/filters"
}