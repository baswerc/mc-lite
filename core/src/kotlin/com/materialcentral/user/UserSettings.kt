package com.materialcentral.user

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import com.beust.klaxon.JsonObject
import org.geezer.json.JsonableObject
import org.geezer.settings.SettingsTable
import org.geezer.toIntList

class UserSettings(
    var defaultRoles: NonEmptyList<UserRole>,
) : JsonableObject {

    constructor(json: JsonObject?) : this(getDefaultRoles(json))


    override fun toJson(): JsonObject {
        return JsonObject().apply {
            this["defaultRoles"] = defaultRoles.map { it.id }
        }
    }

    companion object {
        const val Name = "Users"

        fun get(): UserSettings {
            return UserSettings(SettingsTable.findSettings(Name))
        }

        fun save(userSettings: UserSettings) {
            SettingsTable.saveSettings(Name, userSettings)
        }

        fun getDefaultRoles(settingsJson: JsonObject?): NonEmptyList<UserRole> {
            val roles = settingsJson?.toIntList("defaultRoles")?.mapNotNull { UserRole.mapIfValid(it) }
            return if (roles.isNullOrEmpty()) {
                nonEmptyListOf(UserRole.NO_ACCESS)
            } else {
                roles.toNonEmptyListOrNull()!!
            }
        }
    }
}