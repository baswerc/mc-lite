package com.materialcentral.oss

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.indexWithStandardName
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select

object DigestsTable : DataTable<Digest>("digests") {
    val digest = binary("digest").indexWithStandardName()

    fun getOrCreate(digest: ByteArray): Long {
        return select { DigestsTable.digest eq digest }.singleOrNull()?.let { it[id] } ?: run {
            try {
                create(Digest(digest)).id
            } catch (e: Exception) {
                select { DigestsTable.digest eq digest }.singleOrNull()?.let { it[id] } ?: throw e
            }
        }
    }


    override fun mapDataToStatement(digest: Digest, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[this.digest] = digest.value
    }

    override fun constructData(row: ResultRow): Digest {
        return Digest(row[digest])
    }
}