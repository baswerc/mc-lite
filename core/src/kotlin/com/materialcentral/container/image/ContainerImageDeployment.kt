package com.materialcentral.container.image

import com.materialcentral.environment.EnvironmentDeployment

class ContainerImageDeployment(
    val containerImageId: Long,
    val componentReleaseDeploymentId: Long?,
    environmentId: Long,
    deployedAt: Long,
    undeployedAt: Long?,
    ) : EnvironmentDeployment(environmentId, deployedAt, undeployedAt) {
}