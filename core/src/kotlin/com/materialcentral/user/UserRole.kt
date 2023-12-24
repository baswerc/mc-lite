package com.materialcentral.user

import org.geezer.HasDescription
import org.geezer.db.ReadableDataEnum
import org.geezer.db.ReadableDataEnumType

enum class UserRole(override val id: Int, override val readableId: String, override val label: String, val impliedRoles: List<UserRole>, override val description: String?) : ReadableDataEnum, HasDescription {
    NO_ACCESS(0, "no-access", "No Access", listOf(), ""),
    VIEWER(1, "viewer", "Viewer", listOf(), ""),
    EDITOR(2, "editor", "Editor", listOf(VIEWER), ""),
    SECURITY_OFFICER(4, "security-officer", "Security Officer", listOf(VIEWER), ""),
    ADMINISTRATOR(5, "administrator", "Administrator", listOf(VIEWER, EDITOR, SECURITY_OFFICER), "");

    fun equivalentRoles(): List<UserRole> {
        return listOf(this) + values().filter { it != this && it.impliedRoles.contains(this) }
    }

    companion object : ReadableDataEnumType<UserRole> {
        override val dataEnumValues: Array<UserRole> = enumValues()
    }
}