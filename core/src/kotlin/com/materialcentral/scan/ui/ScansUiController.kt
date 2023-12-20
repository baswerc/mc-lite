package com.materialcentral.scan.ui

import com.materialcentral.DataStringsTable
import org.geezer.io.set
import com.materialcentral.io.ui.*
import org.geezer.io.ui.table.isUiTableRequest
import com.materialcentral.oss.OssPackageCoordinates
import com.materialcentral.oss.OssPackageReleasesTable
import com.materialcentral.oss.OssPackagesTable
import com.materialcentral.oss.OssProjectsTable
import com.materialcentral.scan.Scan
import com.materialcentral.scan.ScansTable
import com.materialcentral.scan.file.KnownVulnerabilityScanFindingsTable
import com.materialcentral.scan.file.OssPackageReleaseScanFindingsTable
import com.materialcentral.vulnerability.KnownVulnerabilitiesTable
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.html.stream.appendHTML
import org.geezer.io.ui.*
import org.geezer.io.warn
import org.geezer.routes.RequestParameters
import org.geezer.routes.ReturnStatus
import org.geezer.routes.TerminateRouteException
import org.jetbrains.exposed.sql.*

object ScansUiController : UiController() {
    override val jspPath: String = "/scans"

    @JvmField
    val GetAllRoute = ::getAll
    fun getAll(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        if (request.isUiTableRequest) {
            ScansUiTable.toHTML(request, parameters, response)
            throw TerminateRouteException()
        }

        return indexJsp
    }

    @JvmField
    val GetScanRoute = ::getScan
    fun getScan(id: Long, request: HttpServletRequest): String {
        return setupViewRequest(id, "attributes.jsp", request)
    }

    @JvmField
    val GetKnownVulnerabilitiesRoute = ::getKnownVulnerabilities
    fun getKnownVulnerabilities(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val scan = id.findOr404(ScansTable)
        val table = KnownVulnerabilityScanFindingsUiTable(id)
        return if (request.isUiTableRequest) {
            table.toHTML(request, parameters, response)
            throw TerminateRouteException(true)
        } else {
            request["table"] = table
            setupViewRequest(scan, component("tablePanel.jsp"), request)
        }
    }

    @JvmField
    val GetKnownVulnerabilitiesBadgeRoute = ::getKnownVulnerabilitiesBadge
    fun getKnownVulnerabilitiesBadge(id: Long, request: HttpServletRequest, response: HttpServletResponse) {
        val scan = id.findOr404(ScansTable)
        response.writer.appendHTML().badge(UI.formatNumber(KnownVulnerabilityScanFindingsTable.select {
            (KnownVulnerabilityScanFindingsTable.scanId eq id) and
            (KnownVulnerabilityScanFindingsTable.findingFilterId eq null)
        }.count()))
    }

    fun getKnownVulnerabilityDetails(scanId: Long, vulnerabilityId: Long, request: HttpServletRequest): String {
        val scan = scanId.findOr404(ScansTable)

        val (scanKnownVulnerability, knownVulnerability) = KnownVulnerabilityScanFindingsTable.innerJoin(KnownVulnerabilitiesTable, { knownVulnerabilityId }, { id }).select {
            (KnownVulnerabilityScanFindingsTable.scanId eq scanId) and (KnownVulnerabilityScanFindingsTable.id eq vulnerabilityId) }
            .singleOrNull()?.let { KnownVulnerabilityScanFindingsTable.map(it) to KnownVulnerabilitiesTable.map(it) } ?: (null to null)

        if (scanKnownVulnerability == null || knownVulnerability == null) {
            throw ReturnStatus.NotFound404
        }

        request.pageObject = scanKnownVulnerability
        request["filePath"] = DataStringsTable.findById(scanKnownVulnerability.filePathId)?.value
        request["ossPackageCoordinates"] = OssPackageCoordinates.findById(scanKnownVulnerability.ossPackageReleaseId)
        request["knownVulnerability"] = knownVulnerability
        return jspp("knownVulnerabilityDetails.jsp")
    }

    @JvmField
    val GetOssPackagesRoute = ::getOssPackages
    fun getOssPackages(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val scan = id.findOr404(ScansTable)
        val table = OssPackageReleaseScanFindingsUiTable(id)
        return if (request.isUiTableRequest) {
            table.toHTML(request, parameters, response)
            throw TerminateRouteException(true)
        } else {
            request["table"] = table
            setupViewRequest(scan, component("tablePanel.jsp"), request)
        }
    }

    @JvmField
    val GetOssPackagesBadgeRoute = ::getOssPackagesBadge
    fun getOssPackagesBadge(id: Long, request: HttpServletRequest, response: HttpServletResponse) {
        val scan = id.findOr404(ScansTable)
        response.writer.appendHTML().badge(UI.formatNumber(OssPackageReleaseScanFindingsTable.select { (OssPackageReleaseScanFindingsTable.scanId eq id) and
                (OssPackageReleaseScanFindingsTable.findingFilterId eq null) }.count()))
    }

    fun getOssPackageDetails(scanId: Long, ossPackageReleaseId: Long, request: HttpServletRequest): String {
        val scan = scanId.findOr404(ScansTable)

        val row = OssPackageReleaseScanFindingsTable.innerJoin(OssPackageReleasesTable, { this.ossPackageReleaseId }, { id })
            .innerJoin(OssPackagesTable, { OssPackageReleasesTable.ossPackageId }, {id}).leftJoin(OssProjectsTable, { OssPackagesTable.projectId }, {id})
            .select {(OssPackageReleaseScanFindingsTable.id eq ossPackageReleaseId) }.singleOrNull()

        if (row == null) {
            throw ReturnStatus.NotFound404
        }

        val finding = OssPackageReleaseScanFindingsTable.map(row)
        request.pageObject = finding
        request["filePath"] = finding?.let { DataStringsTable.findById(it.filePathId)?.value }
        request["package"] = OssPackagesTable.map(row)
        request["release"] = OssPackageReleasesTable.map(row)
        request["project"] = OssProjectsTable.mapOptional(row)
        return jspp("ossPackageDetails.jsp")
    }

    @JvmField
    val PostAbortRoute = ::postAbort
    fun postAbort(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse) {
        val scan = id.findOr404(ScansTable)
        if (scan.state.completed) {
            request.warn("This is scan is already completed.")
        } else {
            scan.abort(message = parameters["message"])
        }

        throw request.redirectToReferrer(response, ::getAll)
    }

    private fun setupViewRequest(id: Long, panelPath: String, request: HttpServletRequest): String {
        val scan = id.findOr404(ScansTable)
        return setupViewRequest(scan, panelPath, request)
    }

    private fun setupViewRequest(scan: Scan, panelPath: String, request: HttpServletRequest): String {
        request.pageObject = scan

        return if (request.hxTab) {
            request.noLayout()
            panelPath
        } else {
            request["showRestart"] = scan.state.completed && request.userSession.hasAnyRole(Scan.ScanRoles)
            request["panel"] = panelPath
            viewJsp
        }
    }
}