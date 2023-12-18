package com.materialcentral.container.image.metadata

class ContainerImageSecret(
    val secretTypeId: Long,
    override val filePathId: Long,
    containerImageId: Long,
    inheritedMetadata: Boolean
) : ContainerImageMetadata(containerImageId, inheritedMetadata) {
}