package com.materialcentral.container.image

import org.geezer.HasLongId
import org.geezer.HasName
import org.geezer.db.cache.DatabaseTransactionCache
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.HasIcon
import org.geezer.io.ui.Linkable
import com.materialcentral.repository.container.ContainerName
import com.materialcentral.repository.container.ContainerRepositoriesTable
import com.materialcentral.repository.container.ContainerRepository
import com.materialcentral.container.image.ui.ContainerImageUiController
import com.materialcentral.repository.container.registry.ContainerRegistriesTable
import com.materialcentral.repository.container.registry.ContainerRegistry
import com.materialcentral.repository.container.registry.ContainerRegistryCache
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import kotlin.reflect.KFunction

class ContainerImageCoordinates(
    val image: ContainerImage,
    val repository: ContainerRepository = ContainerRepositoriesTable.getById(image.containerRepositoryId),
    val registry: ContainerRegistry = ContainerRegistryCache[repository.containerRegistryId]
) : Linkable, HasName, HasIcon, HasLongId {

    val imagePath: ContainerName by lazy { registry.createContainerName(repository, image) }

    val layers: List<ContainerImageLayer> by lazy { ContainerImageLayersTable.findOrderedLayersForImage(image.id) }

    override val route: KFunction<*> = ContainerImageUiController::getImage

    override val id: Long = image.id

    override val name: String
        get() = imagePath.name

    override val linkShortName: String
        get() {
            var repositoryName = repository.name.removeSuffix("/")
            var imageName = image.name
            return if (imageName.startsWith("sha256:")) {
                "$repositoryName@$imageName"
            } else {
                "$repositoryName:$imageName"
            }
        }

    override val icon: FontIcon = ContainerImage.Icon

    operator fun component1(): ContainerRegistry {
        return registry
    }

    operator fun component2(): ContainerRepository {
        return repository
    }

    operator fun component3(): ContainerImage {
        return image
    }

    companion object {
        fun getById(imageId: Long): ContainerImageCoordinates {
            return findById(imageId) ?: throw IllegalArgumentException("Container image with id: $imageId does not exists.")
        }

        fun findById(imageId: Long): ContainerImageCoordinates? {
            return DatabaseTransactionCache.getCachedQuery(ContainerImageCoordinates::class, imageId) {
                ContainerImagesTable.innerJoin(ContainerRepositoriesTable, { containerRepositoryId }, { id }).innerJoin(ContainerRegistriesTable, { ContainerRepositoriesTable.containerRegistryId }, { id }).select { ContainerImagesTable.id eq imageId }.singleOrNull()?.let {
                    ContainerImageCoordinates(ContainerImagesTable.map(it), ContainerRepositoriesTable.map(it), ContainerRegistriesTable.map(it))
                }
            }
        }
    }
}