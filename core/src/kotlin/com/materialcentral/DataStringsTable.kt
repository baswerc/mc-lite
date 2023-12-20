package com.materialcentral

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.uniqueIndexWithStandardName
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select

/**
 * A database pool of shared strings.
 */
object DataStringsTable : DataTable<DataString>("data_strings") {

    const val ValueMaxLength = 5000

    val value = varchar("value", ValueMaxLength).uniqueIndexWithStandardName()

    fun getOrCreate(filePathValue: String): Long {
        val filePathValue = filePathValue.trim()
        return DataStringsTable.select { value eq filePathValue }.singleOrNull()?.let { it[id] } ?: run {
            try {
                DataStringsTable.create(DataString(filePathValue)).id
            } catch (e: Exception) {
                DataStringsTable.select { value eq filePathValue }.singleOrNull()?.let { it[id] } ?: throw e
            }
        }
    }

    override fun mapDataToStatement(dataString: DataString, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[value] = dataString.value
        }
    }

    override fun constructData(row: ResultRow): DataString {
        return DataString(row[value])
    }
}