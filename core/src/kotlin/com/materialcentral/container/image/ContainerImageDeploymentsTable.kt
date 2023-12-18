package com.materialcentral.container.image

import com.materialcentral.component.ComponentReleasesTable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.environment.EnvironmentDeploymentsTable
import com.materialcentral.environment.EnvironmentsTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

object ContainerImageDeploymentsTable : EnvironmentDeploymentsTable<ContainerImageDeployment>("container_images_deployment") {
    val containerImageId = long("container_image_id").referencesWithStandardNameAndIndex(ContainerImagesTable.id, ReferenceOption.CASCADE)

    val componentReleaseDeploymentId = long("component_release_deployment_id").referencesWithStandardNameAndIndex(ComponentReleasesTable.id, ReferenceOption.CASCADE).nullable()

    fun findActiveDeploymentsFor(containerImageId: Long): List<ContainerImageDeployment> {
        return findWhere({(ContainerImageDeploymentsTable.containerImageId eq containerImageId) and (undeployedAt eq null) }, deployedAt to SortOrder.ASC)
    }

    override fun mapDeployment(deployment: ContainerImageDeployment, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[containerImageId] = deployment.containerImageId
            statement[componentReleaseDeploymentId] = deployment.componentReleaseDeploymentId
        }
    }

    override fun constructData(row: ResultRow): ContainerImageDeployment {
        return ContainerImageDeployment(row[containerImageId], row[componentReleaseDeploymentId], row[environmentId], row[deployedAt], row[undeployedAt])
    }
}