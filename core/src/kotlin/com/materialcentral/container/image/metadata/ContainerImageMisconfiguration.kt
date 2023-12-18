package com.materialcentral.container.image.metadata

class ContainerImageMisconfiguration(
    val misconfigurationTypeId: Long,
    override val filePathId: Long,
    containerImageId: Long,
    inheritedMetadata: Boolean
) : ContainerImageMetadata(containerImageId, inheritedMetadata) {
}