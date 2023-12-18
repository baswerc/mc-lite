package com.materialcentral.oss

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.*
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

object OssPackagesTable : DataTable<OssPackage>("oss_packages") {
    val projectId = long("project_id").referencesWithStandardNameAndIndex(OssProjectsTable.id, ReferenceOption.CASCADE).nullable()

    val type = enum("type_id", PackageType)

    val name = varchar("name", 500).indexWithStandardName()

    val approvalState = enum("approval_state_id", OssPackageApprovalState)

    val lastSynchronizedAt = long("last_synchronized_at").nullable()
    init {
        tableUniqueConstraint(type, name)
    }

    fun find(packageType: PackageType, name: String): OssPackage? {
        return select { (type eq packageType) and (OssPackagesTable.name eqIgnoreCase name.trim())}.singleOrNull()?.let(::map)
    }

    fun findOrCreate(packageType: PackageType, name: String): OssPackage {
        val name = name.trim()
        return find(packageType, name) ?: run {
            try {
                create(OssPackage(packageType, name))
            } catch (e: Exception) {
                find(packageType, name) ?: throw e
            }
        }
    }

    override fun mapDataToStatement(ossPackage: OssPackage, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[type] = ossPackage.type
            statement[name] = ossPackage.name
        }
        statement[projectId] = ossPackage.projectId
        statement[approvalState] = ossPackage.approvalState
        statement[lastSynchronizedAt] = ossPackage.lastSynchronizedAt
    }

    override fun constructData(row: ResultRow): OssPackage {
        return OssPackage(row[projectId], row[type], row[name], row[approvalState], row[lastSynchronizedAt])
    }
}