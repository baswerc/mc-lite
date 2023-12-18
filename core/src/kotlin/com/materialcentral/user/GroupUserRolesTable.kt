package com.materialcentral.user

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.list
import org.jetbrains.exposed.sql.ResultRow

object GroupUserRolesTable : DataTable<GroupUserRoles>("user_group_roles") {
    val groupName = varchar("group_name", 100)

    val roles = list("role_ids", UserRole)

    val active = active()

    override fun mapDataToStatement(groupUserRoles: GroupUserRoles, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[groupName] = groupUserRoles.groupName
        statement[roles] = groupUserRoles.roles
        statement[active] = groupUserRoles.active
    }

    override fun constructData(row: ResultRow): GroupUserRoles {
        return GroupUserRoles(row[groupName], row[roles], row[active])
    }
}