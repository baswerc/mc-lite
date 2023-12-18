package com.materialcentral.os

import org.geezer.HasReadableId
import org.geezer.db.DataEnum
import org.geezer.db.ReadableDataEnumType

enum class Architecture(override val id: Int, override val label: String, override val readableId: String = label) : DataEnum, HasReadableId {
    AMD64(0, "amd64"),
    ARM64(1, "arm64"),
    I386(2, "i386"),
    I486(3, "i486"),
    I586(4, "i586"),
    I686(5, "i686");

    companion object : ReadableDataEnumType<Architecture> {
        override val dataEnumValues: Array<Architecture> = values()
    }
}