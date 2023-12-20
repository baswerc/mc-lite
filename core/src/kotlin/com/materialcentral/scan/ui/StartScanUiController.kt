package com.materialcentral.scan.ui

import arrow.core.Either
import com.materialcentral.DataStringsTable
import com.materialcentral.io.ui.UiLogger
import org.geezer.io.set
import org.geezer.io.ui.pageObject
import org.geezer.io.ui.wizard.WizardAction
import org.geezer.io.ui.wizard.WizardMeta
import com.materialcentral.scan.*
import org.geezer.toJsonObject
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.geezer.layouts.Layouts
import org.geezer.routes.RedirectTo
import org.geezer.routes.RequestParameters
import org.geezer.routes.ReturnStatus

object StartScanUiController : ScanConfigurationBuilderUiController() {

    @JvmField
    val StartScanRoute = ::getStartScan
    fun getStartScan(scanTargetTypeId: String, scanTargetId: Long, request: HttpServletRequest): String {
        val scanTargetType = ScanTargetType.mapOptionalReadableId(scanTargetTypeId) ?: throw ReturnStatus.NotFound404
        val scanTarget = scanTargetType.findTarget(scanTargetId) ?: throw ReturnStatus.NotFound404

        request.pageObject = scanTarget
        request["state"] = StartScanWizardState(scanTarget)
        request["meta"] = StartScanWizardMeta

        return jspp("start.jsp")
    }

    fun postConfigure(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val json = parameters["state"]?.toJsonObject()
        if (json == null) {
            UiLogger.warn("Received start scan wizard action with an invalid state object: ${parameters["state"]}", request)
            throw ReturnStatus.BadRequest400
        }

        val state = when (val result = StartScanWizardState.map(json)) {
            is Either.Left -> {
                UiLogger.warn("Received start scan wizard action with an invalid state object: ${parameters["state"]} with error: ${result.value}", request)
                throw ReturnStatus.BadRequest400
            }
            is Either.Right -> result.value
        }

        val scanTarget = state.scanTargetType.findTarget(state.scanTargetId) ?: throw ReturnStatus.BadRequest400

        val action = WizardAction.fromParameters(parameters) ?: WizardAction.NEXT

        request.pageObject = scanTarget

        if (action == WizardAction.CONFIRM) {
            val configuration = ScanConfiguration(state.ignoredPaths, state.analysisConfigurations, state.analysisFindingRetentionCriterion)
            val nameId = DataStringsTable.getOrCreate(scanTarget.getNameForScan())
            val scan = ScansTable.create(Scan(state.scanTargetType, state.scanTargetId, nameId, state.scanMedium!!, configuration, null))

            throw RedirectTo(scan.toUrl(request))
        }

        request[Layouts.LAYOUT] = "wizard"
        request["state"] = state
        request["meta"] = StartScanWizardMeta

        state.updateCurrentState(request, parameters, action)
        return configurationPanel(state.showNextState(request, parameters, action) {
            request["configuration"] = ScanConfiguration(state.ignoredPaths, state.analysisConfigurations, state.analysisFindingRetentionCriterion)
            request["showConfirm"] = true
            "confirmStartScan.jsp"
        })
    }

}

object StartScanWizardMeta : WizardMeta(StartScanUiController::postConfigure, "Start Scan", Scan.Icon)

