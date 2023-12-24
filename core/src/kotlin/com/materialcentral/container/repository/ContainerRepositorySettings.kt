package com.materialcentral.container.repository

import com.beust.klaxon.JsonObject
import org.geezer.json.JsonableObject
import org.geezer.settings.SettingsTable

class ContainerRepositorySettings(
    var minMinutesBetweenNewImagesCheck: Int,
    var minMinutesBetweenFullSynchronization: Int
) : JsonableObject {

    constructor(json: JsonObject?) : this(json?.int("minMinutesBetweenNewImagesCheck") ?: DefaultMinutesBetweenNewImagesCheck,
        json?.int("minMinutesBetweenFullSynchronization") ?: DefaultMinutesBweenFullSynchronization)

    override fun toJson(): JsonObject {
        return JsonObject().apply {
            this["minMinutesBetweenNewImagesCheck"] = minMinutesBetweenNewImagesCheck
            this["minMinutesBetweenFullSynchronization"] = minMinutesBetweenFullSynchronization
        }
    }

    companion object {
        const val Name = "container-repository"

        const val DefaultMinutesBetweenNewImagesCheck = 20

        const val DefaultMinutesBweenFullSynchronization = 360

        fun get(): ContainerRepositorySettings {
            return ContainerRepositorySettings(SettingsTable.findSettings(Name))
        }

        fun save(settings: ContainerRepositorySettings) {
            SettingsTable.saveSettings(Name, settings.toJson())
        }
    }
}