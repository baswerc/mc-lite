package com.materialcentral.oss

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.geezer.db.schema.tableUniqueConstraint
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

object OssPackageReleasesTable : DataTable<OssPackageRelease>("oss_package_releases") {
    val ossPackageId = long("oss_package_id").references(OssPackagesTable.id, onDelete = ReferenceOption.CASCADE, fkName = "material_releases_material_fk").index("material_releases_material_ix")

    val version = varchar("version", 100)

    val majorVersion = long("major_version").nullable()

    val minorVersion = long("minor_version").nullable()

    val patchVersion = long("patch_version").nullable()

    val sizeBytes = integer("size_bytes").nullable()

    val md5DigestId = long("md5_digest_id").referencesWithStandardNameAndIndex(DigestsTable.id, ReferenceOption.NO_ACTION).nullable()

    val sha1DigestId = long("sha1_digest_id").nullable()

    val sha256DigestId = long("sha256_digest_id").nullable()

    val createdAt = long("created_at").nullable()

    init {
        tableUniqueConstraint(ossPackageId, version)
    }

    fun find(ossPackageId: Long, version: String): OssPackageRelease? {
        return OssPackageReleasesTable.select { (OssPackageReleasesTable.ossPackageId eq ossPackageId) and
                (OssPackageReleasesTable.version eq version) }.singleOrNull()?.let { map(it) }
    }

    fun findOrCreate(packageType: PackageType, packageName: String, packageVersion: String): OssPackageRelease {
        val packageName = packageName.trim()
        val ossPackage = OssPackagesTable.findOrCreate(packageType, packageName)
        return findOrCreate(ossPackage.id, packageVersion)
    }

    fun findOrCreate(ossPackageId: Long, version: String): OssPackageRelease {
        val version = version.trim()
        return find(ossPackageId, version) ?: run {
            try {
                create(OssPackageRelease(ossPackageId, version))
            } catch (e: Exception) {
                find(ossPackageId, version) ?: throw e
            }
        }
    }

    override fun mapDataToStatement(release: OssPackageRelease, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[ossPackageId] = release.ossPackageId
            statement[version] = release.version
            statement[majorVersion] = release.majorVersion
            statement[minorVersion] = release.minorVersion
            statement[patchVersion] = release.patchVersion
        }
        statement[md5DigestId] = release.md5DigestId
        statement[sha1DigestId] = release.sha1DigestId
        statement[sha256DigestId] = release.sha256DigestId
        statement[createdAt] = release.createdAt
    }

    override fun constructData(row: ResultRow): OssPackageRelease {
        return OssPackageRelease(row[ossPackageId], row[version], row[majorVersion], row[minorVersion], row[patchVersion], row[sizeBytes], row[md5DigestId], row[sha1DigestId], row[sha256DigestId], row[createdAt])
    }
}