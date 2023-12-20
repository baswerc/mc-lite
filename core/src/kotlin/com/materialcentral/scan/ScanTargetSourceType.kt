package com.materialcentral.scan

import com.materialcentral.container.image.ContainerImagesTable
import com.materialcentral.container.repository.ContainerRepositoriesTable
import org.geezer.db.*

enum class ScanTargetSourceType(override val id: Int, override val readableId: String, override val label: String) : ReadableDataEnum {
    CONTAINER_REPOSITORY(0, "container-repository", "Container Repository"),
    ;

    val scanTargetType: ScanTargetType by lazy { ScanTargetType.values().first { it.scanTargetSourceType == this } }

    fun findTarget(id: Long): ScanTargetSource? {
        return db {
            when (this@ScanTargetSourceType) {
                CONTAINER_REPOSITORY -> {
                    ContainerRepositoriesTable.findById(id)
                }
            }
        }
    }

    companion object : ReadableDataEnumType<ScanTargetSourceType> {
        override val dataEnumValues: Array<ScanTargetSourceType> = entries.toTypedArray()
    }
}