package com.materialcentral.container.image

import com.materialcentral.MaterialsTable
import com.materialcentral.container.repository.ContainerRepositoriesTable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.enum
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.geezer.db.schema.uniqueIndexWithStandardName
import com.materialcentral.os.Architecture
import com.materialcentral.os.LinuxDistribution
import com.materialcentral.os.OperatingSystemType
import com.materialcentral.repository.container.ContainerRepositoriesTable
import org.geezer.db.schema.DataTable
import org.jetbrains.exposed.sql.*

object ContainerImagesTable : DataTable<ContainerImage>("container_images") {

    override val parentId: Column<Long>
        get() = containerRepositoryId

    val containerRepositoryId = long("container_repository_id").referencesWithStandardNameAndIndex(ContainerRepositoriesTable.id, ReferenceOption.CASCADE)

    val digest = varchar("digest", 500).uniqueIndexWithStandardName()

    val name = name()

    val createdAt = long("created_at")

    val baseContainerImageId = long("base_container_image_id").referencesWithStandardNameAndIndex(ContainerImagesTable.id, ReferenceOption.SET_NULL).nullable()

    val baseImage = bool("base_image")

    val os = enum("os_id", OperatingSystemType).nullable()

    val osVersion = varchar("os_version", 100).nullable()

    val linuxDistribution = enum("linux_distribution_id", LinuxDistribution).nullable()

    val architecture = enum("architecture_id", Architecture).nullable()

    val bytesSize = long("bytes_size").nullable()

    val latestInRepository = bool("latest_in_repository")

    val deletedFromRepository = bool("deleted_from_repository")

    fun findLatestImagesFor(containerRepositoryId: Long): List<ContainerImage> {
        return findWhere { (ContainerImagesTable.containerRepositoryId eq containerRepositoryId) and (latestInRepository eq true) }
    }

    fun findByDigest(digest: String): ContainerImage? {
        return ContainerImagesTable.findUniqueWhere { ContainerImagesTable.digest eq digest }
    }

    fun isLatest(containerImageId: Long): Boolean {
        return ContainerImagesTable.slice(latestInRepository).select { id eq containerImageId }.singleOrNull()?.let { it[latestInRepository] } ?: false
    }

    fun isAnyLatest(containerImageIds: Collection<Long>): Boolean {
        return ContainerImagesTable.select { (id inList containerImageIds) and (latestInRepository eq true) }.count() > 0L
    }

    override fun mapMaterialToStatement(image: ContainerImage, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[containerRepositoryId] = image.containerRepositoryId
            statement[digest] = image.digest
        }
        statement[name] = image.name
        statement[createdAt] = image.createdAt
        statement[baseContainerImageId] = image.baseContainerImageId
        statement[baseImage] = image.baseImage
        statement[os] = image.os
        statement[osVersion] = image.osVersion
        statement[linuxDistribution] = image.linuxDistribution
        statement[architecture] = image.architecture
        statement[bytesSize] = image.bytesSize
        statement[latestInRepository] = image.latestInRepository
        statement[deletedFromRepository] = image.deletedFromRepository
    }

    override fun constructData(row: ResultRow): ContainerImage {
        return ContainerImage(row[containerRepositoryId], row[digest], row[name], row[createdAt], row[baseContainerImageId], row[baseImage],
            row[os], row[osVersion], row[linuxDistribution], row[architecture], row[bytesSize], row[latestInRepository], row[deletedFromRepository])
    }
}