package com.materialcentral.scan.ui

import org.geezer.io.ui.UiController

abstract class ScanConfigurationBuilderUiController : UiController() {
    override val jspPath: String = ScansUiController.jspPath

    fun configurationPanel(panel: String): String {
        return jspp("configure/$panel")
    }

}