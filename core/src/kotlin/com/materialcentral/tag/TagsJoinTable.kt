package com.materialcentral.tag

import org.geezer.db.schema.JoinTable
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.select

abstract class TagsJoinTable(name: String) : JoinTable(name) {
    val tagId = long("tag_id").referencesWithStandardNameAndIndex(TagsTable.id, ReferenceOption.CASCADE)

    val idColumn: Column<Long>
        get() {
            val candidateColumns = columns.filter { it != tagId }
            check(candidateColumns.size == 1) { "Invalid tag join columns for table: $tableName"}
            return candidateColumns[0] as Column<Long>
        }

    fun addTagIfNecessary(id: Long, tag: Tag) {
        addTagIfNecessary(id, tag.id)
    }

    fun addTagIfNecessary(id: Long, tagId: Long) {
        addIfNecessary(id, idColumn, tagId, this.tagId)
    }

    fun deleteTagIfNecessary(id: Long, tag: Tag) {
        deleteTagIfNecessary(id, tag.id)
    }

    fun deleteTagIfNecessary(id: Long, tagId: Long) {
        deleteIfNecessary(id, idColumn, tagId, this.tagId)
    }

    fun findTagIdsFor(id: Long): Set<Long> {
        return slice(tagId).select { idColumn eq id }.map { it[tagId] }.toSet()
    }

    fun findCachedTagsFor(id: Long): List<Tag> {
        return TagCache[findTagIdsFor(id)].sorted()
    }
}