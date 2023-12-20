package com.materialcentral.container.image.ui

import com.materialcentral.DataStringsTable
import org.geezer.db.schema.ilike
import org.geezer.io.ui.UI
import org.geezer.io.ui.table.UiColumn
import com.materialcentral.oss.OssPackageReleasesTable
import com.materialcentral.oss.OssPackagesTable
import com.materialcentral.oss.OssProjectsTable
import com.materialcentral.oss.ui.OssPackageReleasesUiTable
import com.materialcentral.container.image.metadata.ContainerImageOssPackageReleasesTable
import com.materialcentral.scan.FindingSeverity
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.span
import org.geezer.routes.RequestParameters
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ContainerImageOssPackageReleasesUiTable(val containerImageId: Long) : OssPackageReleasesUiTable(ContainerImageOssPackageReleasesTable.id) {
    override fun initializeColumns(columns: MutableList<UiColumn>) {
        addStandardColumns(columns)

        columns.add(UiColumn("Critical", icon = FindingSeverity.CRITICAL.icon, tableColumn = ContainerImageOssPackageReleasesTable.criticalFindings) { flowContent, row, request, _ ->
            flowContent.span {
                +(UI.formatNumber(row[ContainerImageOssPackageReleasesTable.criticalFindings]))
            }
        })

        columns.add(UiColumn("High", icon = FindingSeverity.HIGH.icon, tableColumn = ContainerImageOssPackageReleasesTable.highFindings) { flowContent, row, request, _ ->
            flowContent.span {
                +(UI.formatNumber(row[ContainerImageOssPackageReleasesTable.highFindings]))
            }
        })

        columns.add(UiColumn("Medium", icon = FindingSeverity.MEDIUM.icon, tableColumn = ContainerImageOssPackageReleasesTable.mediumFindings) { flowContent, row, request, _ ->
            flowContent.span {
                +(UI.formatNumber(row[ContainerImageOssPackageReleasesTable.mediumFindings]))
            }
        })

        columns.add(UiColumn("Low", icon = FindingSeverity.LOW.icon, tableColumn = ContainerImageOssPackageReleasesTable.lowFindings) { flowContent, row, request, _ ->
            flowContent.span {
                +(UI.formatNumber(row[ContainerImageOssPackageReleasesTable.lowFindings]))
            }
        })

    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        var where: Op<Boolean> = ContainerImageOssPackageReleasesTable.containerImageId eq containerImageId

        if (!searchQuery.isNullOrBlank()) {
            where = where and ((OssPackagesTable.name ilike searchQuery) or (OssPackageReleasesTable.version ilike searchQuery) or
                    (OssProjectsTable.organization ilike searchQuery) or (OssProjectsTable.repository ilike searchQuery) or (DataStringsTable.value ilike searchQuery))
        }

        where = addPackageFilters(where, parameters)

        return ContainerImageOssPackageReleasesTable.innerJoin(OssPackageReleasesTable, { ossPackageReleaseId }, { id }).innerJoin(OssPackagesTable, { OssPackageReleasesTable.ossPackageId }, { id })
            .leftJoin(OssProjectsTable, { OssPackagesTable.projectId }, { id }).leftJoin(DataStringsTable, { ContainerImageOssPackageReleasesTable.filePathId}, { id }).select(where)
    }
}