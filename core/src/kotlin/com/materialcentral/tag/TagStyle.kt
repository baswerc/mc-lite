package com.materialcentral.tag

import org.geezer.db.DataEnum
import org.geezer.db.DataEnumType

enum class TagStyle(override val id: Int, override val label: String, val cssClass: String): DataEnum {
    SOLID(0, "Solid", "tag-solid"),
    OUTLINE(1, "Outline", "tag-outline");
    ;

    companion object : DataEnumType<TagStyle> {
        override val dataEnumValues: Array<TagStyle> = values()
    }
}