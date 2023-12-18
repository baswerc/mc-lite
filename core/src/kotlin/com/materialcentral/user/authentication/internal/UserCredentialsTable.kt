package com.materialcentral.user.authentication.internal

import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.geezer.user.authentication.internal.AppUserCredentialsTable
import org.jetbrains.exposed.sql.ReferenceOption

object UserCredentialsTable : AppUserCredentialsTable() {
    override val userId = long("user_id").referencesWithStandardNameAndIndex(UsersTable.id, ReferenceOption.CASCADE)
}