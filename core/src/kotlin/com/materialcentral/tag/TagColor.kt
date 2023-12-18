package com.materialcentral.tag

import org.geezer.db.DataEnum
import org.geezer.db.DataEnumType

enum class TagColor(override val id: Int, override val label: String, val cssClass: String): DataEnum {
    BLUE(0, "Blue", "tag-blue"),
    DARK_BLUE(1, "Dark Blue", "tag-dark-blue"),
    LIGHT_BLUE(2, "Light Blue", "tag-dark-blue"),
    CYAN(3, "Yellow", "tag-cyan"),
    RED(4, "Red", "tag-red"),
    MAROON(5, "Maroon", "tag-maroon"),
    ORANGE(6, "Orange", "tag-orange"),
    YELLOW(7, "Yellow", "tag-yellow"),
    GREEN(8, "Green", "tag-green"),
    OLIVE(9, "Olive", "tag-olive"),
    LIME(10, "Lime", "tag-lime"),
    PURPLE(11, "Purple", "tag-purple"),
    MAGENTA(12, "Magenta", "tag-magenta"),
    BROWN(13, "Brown", "tag-brown"),
    SILVER(14, "Silver", "tag-silver"),
    GRAY(15, "Gray", "tag-gray"),
    ;

    companion object : DataEnumType<TagColor> {
        override val dataEnumValues: Array<TagColor> = values()
    }
}