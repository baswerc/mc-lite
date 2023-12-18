package com.materialcentral.user

import org.geezer.HasDescription
import org.geezer.db.ReadableDataEnum
import org.geezer.db.ReadableDataEnumType

enum class UserRole(override val id: Int, override val readableId: String, override val label: String, val impliedRoles: List<UserRole>, override val description: String?) : ReadableDataEnum, HasDescription {
    VIEWER(0, "viewer", "Viewer", listOf(), ""),
    EDITOR(1, "editor", "Editor", listOf(VIEWER), ""),
    OWNER(2, "owner", "Owner", listOf(VIEWER, EDITOR), ""),
    SECURITY_OFFICER(3, "security-officer", "Security Officer", listOf(VIEWER), ""),
    ADMINISTRATOR(4, "administrator", "Administrator", listOf(VIEWER, EDITOR, OWNER, SECURITY_OFFICER), "");

    fun equivalentRoles(): List<UserRole> {
        return listOf(this) + values().filter { it != this && it.impliedRoles.contains(this) }
    }

    companion object : ReadableDataEnumType<UserRole> {
        override val dataEnumValues: Array<UserRole> = enumValues()
    }
}