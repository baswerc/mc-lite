package com.materialcentral.oss.ui

import org.geezer.io.ui.table.UiColumn
import org.geezer.io.ui.table.UiTable
import com.materialcentral.oss.*
import kotlinx.html.*
import org.geezer.routes.RequestParameters
import org.geezer.routes.urls.UrlGen
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

abstract class OssPackageReleasesUiTable(rowDetailsColumn: Column<Long>) : UiTable(rowDetailsColumn = rowDetailsColumn) {

    override val defaultSort: List<Pair<Column<*>, SortOrder>> = listOf(OssPackagesTable.type to SortOrder.ASC_NULLS_LAST, OssPackagesTable.name to SortOrder.ASC_NULLS_LAST, OssPackageReleasesTable.version to SortOrder.ASC_NULLS_LAST)

    fun addStandardColumns(columns: MutableList<UiColumn>) {
        addTypeColumn(columns)
        addNameColumn(columns)
        addVersionColumn(columns)
        addProjectColumn(columns)
    }

    fun addTypeColumn(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Type", icon = PackageType.GENERIC.icon, tableColumn = OssPackagesTable.type) { flowContent, row, request, _ ->
            val type = row[OssPackagesTable.type]
            flowContent.span {
                i(type.icon.getCssClass())
                +(" ${type.label}")
            }
        })
    }

    fun addNameColumn(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Package", icon = OssPackage.Icon, tableColumn = OssPackagesTable.name) { flowContent, row, request, _ ->
            flowContent.span {
                flowContent.a(href = UrlGen.url(OssPackageUiController::getPackage, row[OssPackagesTable.id], request)) { +row[OssPackagesTable.name] }
            }
        })
    }

    fun addVersionColumn(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Version", icon = OssPackageRelease.Icon, tableColumn = OssPackageReleasesTable.version) { flowContent, row, request, _ ->
            flowContent.span {
                flowContent.a(href = UrlGen.url(OssPackageUiController::getRelease, row[OssPackageReleasesTable.id], request)) { +row[OssPackageReleasesTable.version] }
            }
        })
    }

    fun addProjectColumn(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Project", icon = OssProject.Icon, tableColumn = OssProjectsTable.id) { flowContent, row, request, _ ->
            val project = OssProjectsTable.mapOptional(row)
            if (project != null) {
                flowContent.span {
                    flowContent.a(href = project.toUrl(request)) { +(project.name) }
                }
            }
        })
    }


    override fun addSearchInput(flowContent: FlowContent, parameters: RequestParameters, tableContainerId: String, searchQuery: String?, placeholder: String) {
        flowContent.div("row") {
            div("col-lg-8") {
                super.addSearchInput(flowContent, parameters, tableContainerId, searchQuery, "Search Known Vulnerabilities")
            }
            div("col-lg-4") {
                select("form-select") {
                    name = PackageTypeParameter
                    multiple = false
                    option {
                        value = ""
                        +"Select Package Type"
                    }
                    val packageTypeIds = parameters.getInts(PackageTypeParameter)
                    for (packageType in PackageType.enumValues) {
                        option {
                            value = packageType.id.toString()
                            selected = packageTypeIds.contains(packageType.id)
                            attributes["autocomplate"] = "off"
                            +packageType.label
                        }
                    }
                }
            }
        }
    }

    protected fun addPackageFilters(query: Op<Boolean>, parameters: RequestParameters): Op<Boolean> {
        var query: Op<Boolean> = query

        val packageTypes = parameters.getInts(PackageTypeParameter).mapNotNull { PackageType.mapOptional(it) }
        if (packageTypes.isNotEmpty()) {
            query = query and (OssPackagesTable.type inList packageTypes)
        }

        return query
    }

    companion object {
        const val PackageTypeParameter = "packageType"
    }
}