package com.materialcentral.os

import org.geezer.HasReadableId
import org.geezer.db.DataEnum
import org.geezer.db.ReadableDataEnumType
import org.geezer.io.ui.FontIcon

enum class OperatingSystemType(override val id: Int, override val label: String, override val readableId: String, val icon: FontIcon) : DataEnum, HasReadableId {
    LINUX(0, "Linux", "linux", FontIcon("fa-linux", "f17c", true)),
    WINDOWS(1, "Windows", "windows", FontIcon("fa-windows", "f17a", true)),
    MAC(2, "Mac", "mac", FontIcon("fa-apple", "f179", true));

    companion object : ReadableDataEnumType<OperatingSystemType> {
        override val dataEnumValues: Array<OperatingSystemType> = values()
    }

}