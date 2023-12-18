package com.materialcentral.db

import com.materialcentral.LocationsTable
import org.geezer.settings.SettingsTable
import com.materialcentral.container.image.ContainerImagesTable
import com.materialcentral.container.registry.ContainerRegistriesTable
import com.materialcentral.container.repository.ContainerRepositoriesTable
import org.geezer.db.schema.AppSchema
import org.geezer.db.schema.View
import org.jetbrains.exposed.sql.Table

object Schema : AppSchema() {
    override val tables: List<Table> = listOf(
        LocationsTable,
        SettingsTable,

        ContainerRegistriesTable,
        ContainerRepositoriesTable,
        ContainerImagesTable
    )

    override val views: List<View<*>>
        get() = TODO("Not yet implemented")
}