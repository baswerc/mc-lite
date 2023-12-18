package com.materialcentral.user

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.db
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.uniqueIndexWithStandardName
import org.geezer.system.runtime.RuntimeClock
import org.geezer.user.AppUsersTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.update

object UsersTable : AppUsersTable<User>() {
    val externalIdentifier = varchar("external_identifier", 100).uniqueIndexWithStandardName().nullable()

    override fun mapUserToStatement(user: User, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[externalIdentifier] = user.externalIdentifier
    }

    override fun constructUser(row: ResultRow, email: String, name: String, active: Boolean, createdAt: Long, lastLoginAt: Long?): User {
        return User(row[externalIdentifier], email, name, createdAt, active, lastLoginAt)
    }
}