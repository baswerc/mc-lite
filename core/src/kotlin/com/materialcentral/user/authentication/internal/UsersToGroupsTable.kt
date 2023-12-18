package com.materialcentral.user.authentication.internal

import com.materialcentral.user.authorization.GroupsTable
import com.materialcentral.user.UsersTable
import org.geezer.db.schema.JoinTable
import org.jetbrains.exposed.sql.ReferenceOption

object UsersToGroupsTable : JoinTable("users_to_groups") {
    val userId = long("user_id").references(UsersTable.id, ReferenceOption.CASCADE)

    val groupId = long("group_id").references(GroupsTable.id, ReferenceOption.CASCADE)
}