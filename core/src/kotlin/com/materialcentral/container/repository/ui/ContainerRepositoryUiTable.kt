package com.materialcentral.container.repository.ui

import com.materialcentral.MaterialGroupType
import com.materialcentral.io.ui.userSession
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.table.UiColumn
import org.geezer.io.ui.table.UiTable
import com.materialcentral.repository.container.ContainerRepositoriesTable
import com.materialcentral.repository.container.ContainerRepository
import com.materialcentral.repository.container.registry.ContainerRegistriesTable
import com.materialcentral.repository.container.registry.ContainerRegistry
import com.materialcentral.repository.container.registry.ContainerRegistryCache
import com.materialcentral.repository.container.registry.ui.ContainerRegistryUiController
import com.materialcentral.user.authorization.Role
import com.materialcentral.user.authorization.andAuthorized
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.*
import org.geezer.db.schema.ilike
import org.geezer.routes.RequestParameters
import org.geezer.routes.urls.UrlGen
import org.jetbrains.exposed.sql.*

object ContainerRepositoryUiTable : UiTable(rowDetailsColumn = ContainerRepositoriesTable.id) {

    const val RegistryParameter = "registry"

    override val defaultSort: List<Pair<Column<*>, SortOrder>> = listOf(ContainerRegistriesTable.hostname to SortOrder.ASC, ContainerRepositoriesTable.name to SortOrder.ASC)

    override fun initializeColumns(columns: MutableList<UiColumn>) {
        columns.add(UiColumn("Repository Path", icon = ContainerRepository.Icon, tableColumn = ContainerRepositoriesTable.name) { flowContent, row, request, _ ->
            flowContent.a(href = UrlGen.url(ContainerRepositoryUiController::getRepository, row[ContainerRepositoriesTable.id], request)) { +row[ContainerRepositoriesTable.name] }
        })

        columns.add(UiColumn("Registry", icon = ContainerRegistry.Icon, tableColumn = ContainerRegistriesTable.hostname) { flowContent, row, request, _ ->
            flowContent.a(href = UrlGen.url(ContainerRegistryUiController::getRegistry, row[ContainerRegistriesTable.id], request)) { +row[ContainerRegistriesTable.hostname] }
        })

        columns.add(UiColumn("Latest Published At", icon = FontIcon.Upload, tableColumn = ContainerRepositoriesTable.latestImageUploadedAt) { flowContent, row, _, _ ->
            val timestamp = row[ContainerRepositoriesTable.latestImageUploadedAt]
            if (timestamp != null) {
                flowContent.div("timestamp") {
                    + timestamp.toString()
                }
            }
        })

    }

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        val baseRepositoryTable = ContainerRepositoriesTable.alias("base")

        var query = ContainerRepositoriesTable.innerJoin(ContainerRegistriesTable, { containerRegistryId }, { id })
            .leftJoin(baseRepositoryTable, { ContainerRepositoriesTable.baseContainerRepositoryId }, { baseRepositoryTable[ContainerRepositoriesTable.id] }).select { (ContainerRepositoriesTable.active eq true) and
                    (ContainerRegistriesTable.active eq true) }

        query = query.andAuthorized(Role.VIEWER, MaterialGroupType.CONTAINER_REPOSITORY, request.userSession)

        if (searchQuery != null) {
            query = query.andWhere {
                var where: Op<Boolean> = (ContainerRepositoriesTable.name ilike  searchQuery)
                where
            }
        }

        val registryId = parameters.getLong(RegistryParameter)
        if (registryId != null) {
            query = query.andWhere { ContainerRegistriesTable.id eq registryId }
        }

        return query
    }

    override fun addSearchInput(flowContent: FlowContent, parameters: RequestParameters, tableContainerId: String, searchQuery: String?, placeholder: String) {
        val registries = ContainerRegistryCache.getAll().sortedBy { it.hostname }
        if (registries.size <= 1) {
            super.addSearchInput(flowContent, parameters, tableContainerId, searchQuery, "Search Container Repositories")
        } else {
            flowContent.div("row") {
                div("col-lg-8") {
                    super.addSearchInput(flowContent, parameters, tableContainerId, searchQuery, "Search Container Repositories")
                }
                div("col-lg-4") {
                    select("form-select") {
                        name = RegistryParameter
                        option {
                            +"Select Container Registry"
                        }
                        val selectedRegistryId = parameters.getLong(RegistryParameter)
                        for (registry in registries) {
                            option {
                                value = registry.id.toString()
                                selected = registry.id == selectedRegistryId
                                +registry.hostname
                            }
                        }
                    }
                }
            }
        }
    }
}