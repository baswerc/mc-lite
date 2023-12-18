package com.materialcentral.user.authorization

import com.materialcentral.user.UserRole
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.list
import org.geezer.db.schema.uniqueIndexWithStandardName
import org.jetbrains.exposed.sql.ResultRow

object GroupsTable : DataTable<Group>("groups") {
    val name = name().uniqueIndexWithStandardName()

    val description = description()

    val memberRoles = list("member_role_ids", UserRole)

    val active = active()

    val createdAt = createdAt()

    override fun mapDataToStatement(group: Group, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[createdAt] = group.createdAt
        }
        statement[name] = group.name
        statement[description] = group.description
        statement[memberRoles] = group.memberRoles
        statement[active] = group.active
    }

    override fun constructData(row: ResultRow): Group {
        return Group(row[name], row[description], row[memberRoles], row[active], row[createdAt])
    }
}