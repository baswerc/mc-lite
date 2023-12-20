package com.materialcentral.scan.ui

import com.materialcentral.DataStringsTable
import org.geezer.db.schema.ilike
import org.geezer.io.ui.UI
import org.geezer.io.ui.table.UiColumn
import com.materialcentral.oss.OssPackageReleasesTable
import com.materialcentral.oss.OssPackagesTable
import com.materialcentral.oss.OssProjectsTable
import com.materialcentral.oss.ui.OssPackageReleasesUiTable
import com.materialcentral.scan.FindingSeverity
import com.materialcentral.scan.file.OssPackageReleaseScanFindingsTable
import com.materialcentral.scan.filter.ScanFindingFilter
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.span
import org.geezer.routes.RequestParameters
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class OssPackageReleaseScanFindingsUiTable(val scanId: Long) : OssPackageReleasesUiTable(OssPackageReleaseScanFindingsTable.id) {
    override fun initializeColumns(columns: MutableList<UiColumn>) {
        addStandardColumns(columns)

        columns.add(UiColumn("Filtered", icon = ScanFindingFilter.Icon, tableColumn = OssPackageReleaseScanFindingsTable.findingFilterId) { html, row, request, _ ->
            html.span {
                +(UI.formatBoolean(row[OssPackageReleaseScanFindingsTable.findingFilterId] != null))
            }
        })

        columns.add(UiColumn("Critical", icon = FindingSeverity.CRITICAL.icon, tableColumn = OssPackageReleaseScanFindingsTable.criticalFindings) { html, row, request, _ ->
            html.span {
                +(UI.formatNumber(row[OssPackageReleaseScanFindingsTable.criticalFindings]))
            }
        })

        columns.add(UiColumn("High", icon = FindingSeverity.HIGH.icon, tableColumn = OssPackageReleaseScanFindingsTable.highFindings) { html, row, request, _ ->
            html.span {
                +(UI.formatNumber(row[OssPackageReleaseScanFindingsTable.highFindings]))
            }
        })

        columns.add(UiColumn("Medium", icon = FindingSeverity.MEDIUM.icon, tableColumn = OssPackageReleaseScanFindingsTable.mediumFindings) { html, row, request, _ ->
            html.span {
                +(UI.formatNumber(row[OssPackageReleaseScanFindingsTable.mediumFindings]))
            }
        })

        columns.add(UiColumn("Low", icon = FindingSeverity.LOW.icon, tableColumn = OssPackageReleaseScanFindingsTable.lowFindings) { html, row, request, _ ->
            html.span {
                +(UI.formatNumber(row[OssPackageReleaseScanFindingsTable.lowFindings]))
            }
        })

    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        var where: Op<Boolean> = OssPackageReleaseScanFindingsTable.scanId eq scanId

        if (!searchQuery.isNullOrBlank()) {
            where = where and ((OssPackagesTable.name ilike searchQuery) or (OssPackageReleasesTable.version ilike searchQuery) or
                    (OssProjectsTable.organization ilike searchQuery) or (OssProjectsTable.repository ilike searchQuery) or (DataStringsTable.value ilike searchQuery))
        }

        where = addPackageFilters(where, parameters)

        return OssPackageReleaseScanFindingsTable.innerJoin(OssPackageReleasesTable, { ossPackageReleaseId }, { id }).innerJoin(OssPackagesTable, { OssPackageReleasesTable.ossPackageId }, { id })
            .leftJoin(OssProjectsTable, { OssPackagesTable.projectId }, { id }).leftJoin(DataStringsTable, { OssPackageReleaseScanFindingsTable.filePathId}, { id }).select(where)
    }
}