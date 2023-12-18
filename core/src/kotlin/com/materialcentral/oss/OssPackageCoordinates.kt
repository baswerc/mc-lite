package com.materialcentral.oss

import com.github.packageurl.PackageURLBuilder
import org.geezer.db.cache.DatabaseTransactionCache
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select

class OssPackageCoordinates(
    val ossPackage: OssPackage,
    val ossPackageRelease: OssPackageRelease
) {

    val purl: String by lazy {
        PackageURLBuilder.aPackageURL().withType(ossPackage.type.purlType).withName(ossPackage.name).withVersion(ossPackageRelease.version).build().canonicalize()
    }

    companion object {
        fun findById(ossPackageReleaseId: Long): OssPackageCoordinates? {
            return DatabaseTransactionCache.getCachedQuery(OssPackageCoordinates::class, ossPackageReleaseId) {
                OssPackageReleasesTable.innerJoin(OssPackagesTable, { ossPackageId }, { id }).select { OssPackageReleasesTable.id eq ossPackageReleaseId }.singleOrNull()?.let { OssPackageCoordinates(OssPackagesTable.map(it), OssPackageReleasesTable.map(it)) }
            }
        }
    }
}