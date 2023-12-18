package com.materialcentral.tag

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.enum
import org.geezer.db.schema.eqIgnoreCase
import org.geezer.db.schema.uniqueIndexWithStandardName
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select

object TagsTable : DataTable<Tag>("tags") {
    val name = name().uniqueIndexWithStandardName()

    val description = description()

    val color: Column<TagColor> = enum("color_id", TagColor)

    val style: Column<TagStyle> = enum("style_id", TagStyle)

    fun findByName(tagName: String): Tag? {
        return select { name eqIgnoreCase tagName }.singleOrNull()?.let(::constructData)
    }
    override fun mapDataToStatement(tag: Tag, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[name] = tag.name
        statement[description] = tag.description
        statement[color] = tag.color
        statement[style] = tag.style
    }

    override fun constructData(row: ResultRow): Tag {
        return Tag(row[name], row[description], row[color], row[style])
    }
}