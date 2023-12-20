package com.materialcentral.scan

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import com.materialcentral.container.image.ContainerImage
import com.materialcentral.container.image.ContainerImagesTable
import com.materialcentral.container.image.ui.ContainerImagesUiController
import org.geezer.db.ReadableDataEnum
import org.geezer.db.ReadableDataEnumType
import org.geezer.db.db
import org.geezer.io.ui.FontIcon
import kotlin.reflect.KFunction

enum class ScanTargetType(override val id: Int, override val readableId: String, override val label: String, val scanMediums: NonEmptyList<ScanMedium>, val icon: FontIcon, val scanTargetSourceType: ScanTargetSourceType, val route: KFunction<*>) : ReadableDataEnum {
    CONTAINER_IMAGE(0, "container-image", "Container Image", nonEmptyListOf(ScanMedium.METADATA, ScanMedium.ASSET), ContainerImage.Icon, ScanTargetSourceType.CONTAINER_REPOSITORY, ContainerImagesUiController::getImage)
    ;

    fun findTarget(id: Long): ScanTarget? {
        return db {
            when (this@ScanTargetType) {
                CONTAINER_IMAGE -> {
                    ContainerImagesTable.findById(id)
                }
            }
        }
    }

    companion object : ReadableDataEnumType<ScanTargetType> {
        @JvmField
        val Icon = FontIcon("fa-bullseye", "f140")

        override val dataEnumValues: Array<ScanTargetType> = enumValues()
    }
}