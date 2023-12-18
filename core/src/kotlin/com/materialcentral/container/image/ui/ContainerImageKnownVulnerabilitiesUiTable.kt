package com.materialcentral.container.image.ui

import com.materialcentral.LocationsTable
import org.geezer.db.schema.ilike
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.UI
import org.geezer.io.ui.table.UiColumn
import com.materialcentral.oss.OssPackage
import com.materialcentral.oss.OssPackageReleasesTable
import com.materialcentral.oss.OssPackagesTable
import com.materialcentral.container.image.metadata.ContainerImageKnownVulnerabilitiesTable
import com.materialcentral.vulnerability.KnownVulnerabilitiesTable
import com.materialcentral.vulnerability.ui.KnownVulnerabilityUiTable
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.div
import kotlinx.html.i
import org.geezer.routes.RequestParameters
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ContainerImageKnownVulnerabilitiesUiTable(val containerImageId: Long) : KnownVulnerabilityUiTable(ContainerImageKnownVulnerabilitiesTable.id) {

    override fun initializeColumns(columns: MutableList<UiColumn>) {

        addIdentifierColumn(columns)

        columns.add(UiColumn("Package", icon = OssPackage.Icon, tableColumns = listOf(OssPackagesTable.name, OssPackageReleasesTable.version) ) { flowContent, row, request, rowCache ->
            val packageType = row[OssPackagesTable.type]
            flowContent.div {
                flowContent.i(packageType.icon.getCssClass())
                +" ${row[OssPackagesTable.name]}@${row[OssPackageReleasesTable.version]}"
            }
        })

        addAverageCvssColumn(columns)

        addEpssColumn(columns)

        columns.add(UiColumn("Inherited", icon = FontIcon.Description, tableColumn = KnownVulnerabilitiesTable.title) { flowContent, row, _, _ ->
            flowContent.text(UI.formatBoolean(row[ContainerImageKnownVulnerabilitiesTable.inheritedMetadata]))
        })
    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        var where: Op<Boolean> = ContainerImageKnownVulnerabilitiesTable.containerImageId eq containerImageId

        if (!searchQuery.isNullOrBlank()) {
            where = where and ((KnownVulnerabilitiesTable.title ilike searchQuery) or (KnownVulnerabilitiesTable.description ilike searchQuery) or (LocationsTable.location ilike searchQuery))
        }

        where = addVulnerabilityFilters(where, parameters)


        return ContainerImageKnownVulnerabilitiesTable.innerJoin(KnownVulnerabilitiesTable, { knownVulnerabilityId }, { id }).innerJoin(OssPackageReleasesTable, { ContainerImageKnownVulnerabilitiesTable.ossPackageReleaseId }, { id })
            .innerJoin(OssPackagesTable, { OssPackageReleasesTable.ossPackageId }, { id }).leftJoin(LocationsTable, {ContainerImageKnownVulnerabilitiesTable.filePathId}, { id }).select(where)
    }
}