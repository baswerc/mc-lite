package com.materialcentral.container.image

import org.geezer.db.Data
import org.geezer.io.ui.FontIcon

class ContainerImageTag(val containerImageId: Long,
                        val value: String,
                        val addedAt: Long,
                        var removedAt: Long?) : Data() {


    companion object {
        @JvmField
        val Icon = FontIcon("fa-tag", "f02b")
    }
}