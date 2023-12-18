package com.materialcentral.user

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.uniqueIndexWithStandardName
import org.geezer.user.AppUsersTable
import org.jetbrains.exposed.sql.ResultRow

object UsersTable : AppUsersTable<User>() {
    val username = varchar("username", 100).uniqueIndexWithStandardName().nullable()

    override fun mapUserToStatement(user: User, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[username] = user.username
    }

    override fun constructUser(row: ResultRow, email: String, name: String, active: Boolean, createdAt: Long, lastLoginAt: Long?): User {
        return User(row[username], email, name, createdAt, active, lastLoginAt)
    }
}