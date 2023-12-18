package com.materialcentral.container.image.metadata

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.container.image.ContainerImagesTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption

abstract class ContainerImageMetadataTable<M : ContainerImageMetadata>(name: String) : DataTable<M>(name) {

    abstract val metadataId: Column<Long>

    abstract val filePathId: Column<*>

    abstract fun mapMetadata(metadata: M, statement: FilteredUpdateStatement, insert: Boolean)

    val containerImageId = long("container_image_id").referencesWithStandardNameAndIndex(ContainerImagesTable.id, ReferenceOption.CASCADE)

    val inheritedMetadata = bool("inherited_metadata")

    fun findForImage(imageId: Long): List<M> {
        return findWhere { containerImageId eq imageId }
    }

    final override fun mapDataToStatement(metadata: M, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[containerImageId] = metadata.containerImageId
        }
        statement[inheritedMetadata] = metadata.inheritedMetadata
        mapMetadata(metadata, statement, insert)
    }
}