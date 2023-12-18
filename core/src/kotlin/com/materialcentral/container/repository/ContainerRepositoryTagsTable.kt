package com.materialcentral.container.repository

import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.tag.TagsJoinTable
import org.jetbrains.exposed.sql.ReferenceOption

object ContainerRepositoryTagsTable : TagsJoinTable("container_repository_tags") {
    val containerRepositoryId = long("container_repository_id").referencesWithStandardNameAndIndex(ContainerRepositoriesTable.id, ReferenceOption.CASCADE)

    init {
        joinTableUniqueConstraint()
    }
}