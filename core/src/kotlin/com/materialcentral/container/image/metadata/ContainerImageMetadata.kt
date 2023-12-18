package com.materialcentral.container.image.metadata

import org.geezer.db.Data

abstract class ContainerImageMetadata(
    val containerImageId: Long,
    var inheritedMetadata: Boolean
) : Data() {

    abstract val filePathId: Long?

}