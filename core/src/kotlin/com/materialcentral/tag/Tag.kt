package com.materialcentral.tag

import org.geezer.db.Data
import org.geezer.io.ui.escapeHtml

class Tag(
    var name: String,
    var description: String?,
    var color: TagColor,
    var style: TagStyle
) : Data(), Comparable<Tag> {

    override fun compareTo(other: Tag): Int {
        return name.lowercase().compareTo(other.name.lowercase())
    }

    fun toHtml(): String {
        return """<span class="tag ${color.cssClass} ${style.cssClass}" title="${description.escapeHtml()}">${name.escapeHtml()}</span>"""
    }
}