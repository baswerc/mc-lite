package com.materialcentral.container.repository.ui

import com.materialcentral.container.image.ContainerImagesTable
import com.materialcentral.container.image.ui.BaseImagesUiTable
import com.materialcentral.container.repository.ContainerRepositoriesTable
import com.materialcentral.container.repository.ContainerRepository
import jakarta.servlet.http.HttpServletRequest
import org.geezer.routes.RequestParameters
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select

class ContainerRepositoryImagesUiTable(val containerRepository: ContainerRepository) : BaseImagesUiTable() {

    override fun createQuery(request: HttpServletRequest, parameters: RequestParameters, searchQuery: String?): Query {
        var query = ContainerImagesTable.innerJoin(ContainerRepositoriesTable, { containerRepositoryId }, { id }).select { ContainerImagesTable.containerRepositoryId eq containerRepository.id }

        if (searchQuery != null) {
            query = query.andWhere { createSearchQueryFilter(searchQuery) }
        }

        return query
    }
}