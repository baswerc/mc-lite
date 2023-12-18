package com.materialcentral.user.authorization.internal

import com.materialcentral.user.UsersTable
import com.materialcentral.user.authorization.GroupsTable
import org.geezer.db.schema.JoinTable
import org.jetbrains.exposed.sql.ReferenceOption

object GroupsToUsersTable : JoinTable("groups_to_users") {
    val groupId = long("group_id").references(GroupsTable.id, ReferenceOption.CASCADE)

    val userId = long("user_id").references(UsersTable.id, ReferenceOption.CASCADE)
}