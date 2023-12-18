package com.materialcentral.container.image

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.materialcentral.job.JobWorker
import com.materialcentral.os.Architecture
import com.materialcentral.os.OperatingSystemType
import com.materialcentral.repository.container.registry.client.ContainerRegistryClient
import com.materialcentral.repository.container.registry.client.ImageMetadata
import com.materialcentral.repository.container.registry.client.TagMetadata
import com.materialcentral.repository.container.registry.oci.manifest.Manifest
import org.geezer.system.runtime.RuntimeClock
import org.jetbrains.exposed.sql.*

object ContainerImageSynchronizer : JobWorker<ContainerImageSynchronization>(ContainerImageSynchronization::class, ContainerImageSynchronizationsTable) {

    override fun work(job: ContainerImageSynchronization) {
        val coordinates = ContainerImageCoordinates.findById(job.containerImageId)
        if (coordinates == null) {
            job.failed("Unable to find image with id: ${job.containerImageId}")
            return
        }

        Thread.sleep(60_000)

        val (registry, repository, image) = coordinates
        val synchronizationAt = RuntimeClock.now
        ContainerImagesTable.lockRow(image.id) {
            val metadata = ContainerRegistryClient.getImageMetadata(registry, repository, image)
            val allTags = ContainerRegistryClient.getTags(registry, repository)

            synchronizeProperties(image, metadata)
            synchronizeTags(image, metadata, allTags, synchronizationAt)

            if (metadata != null) {
                synchronizeLayers(image, metadata.manifest)
            }
        }
    }

    fun synchronizeProperties(image: ContainerImage, metadata: ImageMetadata?) {
        var propertyUpdated = false

        if (metadata == null) {
            if (!image.deletedFromRepository) {
                image.deletedFromRepository = true
                propertyUpdated = true
            }
        } else {
            if (image.deletedFromRepository) {
                log.warn("Container image $image was previously marked as being deleted from repository but now is not.")
                image.deletedFromRepository = false
                propertyUpdated = true
            }

            val lastModifiedAt = metadata.manifest.lastModifiedAt
            if (lastModifiedAt != null && image.createdAt != lastModifiedAt) {
                image.createdAt = lastModifiedAt
                propertyUpdated = true
            }

            if (image.architecture == null && !metadata.architecture.isNullOrBlank()) {
                val architecture = Architecture.mapOptionalReadableId(metadata.architecture)
                if (architecture != null) {
                    image.architecture = architecture
                    propertyUpdated = true
                } else {
                    log.warn("Unable to map architecture: ${metadata.architecture} for image: $image")
                }
            }

            if (image.os == null && !metadata.os.isNullOrBlank()) {
                val os = OperatingSystemType.mapOptionalReadableId(metadata.os)
                if (os != null) {
                    image.os = os
                    propertyUpdated = true
                } else {
                    log.warn("Unable to map os: ${metadata.os} for image: $image")
                }
            }

            if (image.osVersion.isNullOrBlank() && !metadata.osVersion.isNullOrBlank()) {
                image.osVersion = metadata.osVersion
                propertyUpdated = true
            }

            val size = metadata.manifest.layers.mapNotNull { it.size }.sum()
            if (image.bytesSize != size) {
                image.bytesSize = size
                propertyUpdated = true
            }
        }


        if (propertyUpdated) {
            ContainerImagesTable.update(image, ContainerImagesTable.deletedFromRepository, ContainerImagesTable.createdAt, ContainerImagesTable.architecture, ContainerImagesTable.os, ContainerImagesTable.os, ContainerImagesTable.bytesSize)
        }
    }


    fun synchronizeTags(image: ContainerImage, metadata: ImageMetadata?, allTags: List<TagMetadata>?, synchronizationStartedAt: Long) {
        val imageTags = ContainerImageTagsTable.getActiveTagsForImage(image.id)

        for (imageTag in imageTags) {
            val tagMeta = metadata?.tags?.firstOrNull { it.value == imageTag.value }
            if (tagMeta == null) {
                val removedAt = allTags?.firstOrNull { it.value == imageTag.value }?.taggedAt ?: synchronizationStartedAt
                ContainerImageTagsTable.update({ ContainerImageTagsTable.id eq imageTag.id}) {
                    it[ContainerImageTagsTable.removedAt] = removedAt
                }
            }
        }

        if (metadata != null) {
            for (tagMeta in metadata.tags) {
                val imageTag = imageTags.firstOrNull { it.value == tagMeta.value }
                if (imageTag == null) {
                    ContainerImageTagsTable.create(ContainerImageTag(image.id, tagMeta.value, tagMeta.taggedAt ?: synchronizationStartedAt, null))
                }
            }

            val name = metadata.tags.sortedWith(compareByDescending<TagMetadata> { it.taggedAt ?: 0 }.thenBy() { it.value })?.firstOrNull()?.value ?: ContainerImage.shortenDigest(metadata.manifest.digest)
            if (image.name != name) {
                image.name = name
                ContainerImagesTable.update(image, ContainerImagesTable.name)
            }
        } else {
            val name = ContainerImage.shortenDigest(image.digest)
            if (image.name != name) {
                image.name = name
                ContainerImagesTable.update(image, ContainerImagesTable.name)
            }
        }
    }

    fun synchronizeLayers(image: ContainerImage, manifest: Manifest) {

        var layers = ContainerImageLayersTable.findOrderedLayersForImage(image.id)
        val metadataLayers = manifest.layers

        var createLayersNeeded = layers.size != metadataLayers.size
        if (!createLayersNeeded) {
            for ((index, layer) in layers.withIndex()) {
                val metadataLayer = metadataLayers[index]
                if (metadataLayer.digest != layer.digest) {
                    createLayersNeeded = true
                    break
                }
            }
        }

        if (createLayersNeeded) {
            for (layer in layers) {
                ContainerImageLayersTable.delete(layer)
            }

            val newLayers = mutableListOf<ContainerImageLayer>()
            for ((index, layer) in metadataLayers.withIndex()) {
                val lastLayer = index == metadataLayers.size - 1
                newLayers.add(ContainerImageLayersTable.create(ContainerImageLayer(image.id, false, index, layer.digest, layer.size ?: 0, lastLayer)))
            }
            layers = newLayers
        }


        for (index in layers.sortedBy { it.index }.indices.reversed()) {
            val layer = layers[index]
            if (layer.lastLayer) {
                continue
            }

            var layersFilter: Op<Boolean> = exists(ContainerImageLayersTable.select { (ContainerImageLayersTable.containerImageId eq ContainerImagesTable.id) and
                    (ContainerImageLayersTable.index eq index) and (ContainerImageLayersTable.digest eq layer.digest) and (ContainerImageLayersTable.lastLayer eq true) })

            var nextIndex = index - 1
            while (nextIndex >= 0) {
                val nextLayer = layers[nextIndex]
                layersFilter = layersFilter and exists(ContainerImageLayersTable.select { (ContainerImageLayersTable.containerImageId eq ContainerImagesTable.id) and
                        (ContainerImageLayersTable.index eq nextIndex) and (ContainerImageLayersTable.digest eq nextLayer.digest ) })
                --nextIndex
            }


            val parentCandidates = ContainerImagesTable.findWhere { layersFilter }

            if (parentCandidates.isNotEmpty()) {
                val parentImage = pickBestLayerParentCandidate(image, parentCandidates.toNonEmptyListOrNull()!!)

                if (image.baseContainerImageId != parentImage.id) {
                    image.baseContainerImageId = parentImage.id
                    ContainerImagesTable.update(image, ContainerImagesTable.baseContainerImageId)
                }

                if (!parentImage.baseImage) {
                    parentImage.baseImage = true
                    ContainerImagesTable.update(parentImage, ContainerImagesTable.baseImage)
                }

                for (j in index downTo 0) {
                    val nextLayer = layers[j]
                    nextLayer.parentLayer = true
                    ContainerImageLayersTable.update(nextLayer, ContainerImageLayersTable.parentLayer)
                }

            }
        }
    }

    fun pickBestLayerParentCandidate(image: ContainerImage, candidates: NonEmptyList<ContainerImage>): ContainerImage {
        val candidates = candidates.sortedByDescending { it.createdAt }
        var selected = candidates.filter { it.os == image.os && it.linuxDistribution == image.linuxDistribution && it.architecture == image.architecture }
        if (selected.isNotEmpty()) {
            return selected[0]
        }

        selected = candidates.filter { it.os == image.os && it.architecture == image.architecture }
        if (selected.isNotEmpty()) {
            return selected[0]
        }

        selected = candidates.filter { it.architecture == image.architecture }
        if (selected.isNotEmpty()) {
            return selected[0]
        }

        selected = candidates.filter { it.os == image.os }
        if (selected.isNotEmpty()) {
            return selected[0]
        }

        return candidates[0]
    }
}