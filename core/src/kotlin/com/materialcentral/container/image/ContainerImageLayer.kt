package com.materialcentral.container.image

import org.geezer.db.Data
import org.geezer.io.ui.FontIcon

class ContainerImageLayer(
    val containerImageId: Long,
    var parentLayer: Boolean,
    val index: Int,
    val digest: String,
    val bytesSize: Long,
    val lastLayer: Boolean,
) : Data() {

    companion object {
        @JvmField
        val Icon = FontIcon("fa-square", "f0c8")
    }
}