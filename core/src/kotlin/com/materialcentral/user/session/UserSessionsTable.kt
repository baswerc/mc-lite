package com.materialcentral.user.session

import org.geezer.db.FilteredUpdateStatement
import com.materialcentral.user.UserRole
import com.materialcentral.user.UsersTable
import org.geezer.db.schema.*
import org.geezer.user.authentication.UserAuthenticationType
import org.geezer.user.session.AppUserSessionsTable
import org.jetbrains.exposed.sql.*

object UserSessionsTable : AppUserSessionsTable<UserSession>() {

    override val userId: Column<Long> = long("user_id").referencesWithStandardNameAndIndex(UsersTable.id, ReferenceOption.CASCADE)

    val groups = stringList("groups")

    val roles = list("user_role_ids", UserRole)

    override fun mapUserSessionToStatement(userSession: UserSession, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[groups] = userSession.groups
            statement[roles] = userSession.roles
        }
    }

    override fun constructUserSession(row: ResultRow, identifier: String, userId: Long, authenticationType: UserAuthenticationType, userAgentHash: ByteArray, ipAddressHash: ByteArray,
        createdAt: Long, lastAccessedAt: Long, active: Boolean ): UserSession {
        return UserSession(row[groups], row[roles], identifier, userId, authenticationType, userAgentHash, ipAddressHash, createdAt, lastAccessedAt, active)
    }
}