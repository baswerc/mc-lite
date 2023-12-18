package com.materialcentral

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.uniqueIndexWithStandardName
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select

object LocationsTable : DataTable<Location>("location") {

    const val LocationMaxLength = 4000

    val location = varchar("location", LocationMaxLength).uniqueIndexWithStandardName()

    fun getOrCreate(filePathValue: String): Long {
        val filePathValue = filePathValue.trim()
        return LocationsTable.select { location eq filePathValue }.singleOrNull()?.let { it[id] } ?: run {
            try {
                LocationsTable.create(Location(filePathValue)).id
            } catch (e: Exception) {
                LocationsTable.select { location eq filePathValue }.singleOrNull()?.let { it[id] } ?: throw e
            }
        }
    }

    override fun mapDataToStatement(location: Location, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[LocationsTable.location] = location.value
        }
    }

    override fun constructData(row: ResultRow): Location {
        return Location(row[location])
    }
}