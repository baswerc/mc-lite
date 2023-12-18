package com.materialcentral.container.image

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.indexWithStandardName
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select

object ContainerImageLayersTable : DataTable<ContainerImageLayer>("container_image_layers") {
    val containerImageId = long("container_image_id").referencesWithStandardNameAndIndex(ContainerImagesTable.id, ReferenceOption.CASCADE)

    val parentLayer = bool("parent_layer")

    val index = integer("index")

    val digest = varchar("digest", 100).indexWithStandardName()

    val bytesSize = long("bytes_size")

    val lastLayer = bool("last_layer")

    override fun mapDataToStatement(layer: ContainerImageLayer, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[containerImageId] = layer.containerImageId
            statement[index] = layer.index
            statement[digest] = layer.digest
            statement[bytesSize] = layer.bytesSize
            statement[lastLayer] = layer.lastLayer
        }
        statement[parentLayer] = layer.parentLayer
    }

    override fun constructData(row: ResultRow): ContainerImageLayer {
        return ContainerImageLayer(row[containerImageId], row[parentLayer], row[index], row[digest], row[bytesSize], row[lastLayer])
    }

    fun findOrderedLayersForImage(containerImageId: Long): List<ContainerImageLayer> {
        return select { ContainerImageLayersTable.containerImageId eq containerImageId }.orderBy(index, SortOrder.ASC).map(::constructData)
    }
}