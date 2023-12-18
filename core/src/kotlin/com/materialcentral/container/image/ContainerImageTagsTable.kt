package com.materialcentral.container.image

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.indexWithStandardName
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and

object ContainerImageTagsTable : DataTable<ContainerImageTag>("container_image_tags") {

    val containerImageId = long("container_image_id").referencesWithStandardNameAndIndex(ContainerImagesTable.id, ReferenceOption.CASCADE)

    val value = varchar("value", 150).indexWithStandardName()

    val addedAt = long("added_at").indexWithStandardName()

    val removedAt = long("removed_at").nullable().indexWithStandardName()

    fun getActiveTagsForImage(id: Long): List<ContainerImageTag> {
        return findWhere { (containerImageId eq id) and (removedAt eq null) }
    }

    override fun mapDataToStatement(imageTag: ContainerImageTag, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[containerImageId] = imageTag.containerImageId
            statement[value] = imageTag.value
            statement[addedAt] = imageTag.addedAt
        }
        statement[removedAt] = imageTag.removedAt
    }

    override fun constructData(row: ResultRow): ContainerImageTag {
        return ContainerImageTag(row[containerImageId], row[value], row[addedAt], row[removedAt])
    }
}