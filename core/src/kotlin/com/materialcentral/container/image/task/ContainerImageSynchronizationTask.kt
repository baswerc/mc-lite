package com.materialcentral.container.image.task

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.materialcentral.container.client.ContainerRegistryClient
import com.materialcentral.container.client.ImageMetadata
import com.materialcentral.container.client.TagMetadata
import com.materialcentral.container.image.*
import com.materialcentral.container.oci.manifest.Manifest
import com.materialcentral.os.Architecture
import com.materialcentral.os.OperatingSystemType
import org.geezer.io.ui.attribute
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.TagConsumer
import kotlinx.html.div
import org.geezer.system.runtime.DataEnumProperty
import org.geezer.system.runtime.RuntimeClock
import org.geezer.task.Task
import org.geezer.task.TaskInitializationParameters
import org.geezer.task.TaskPriority
import org.geezer.task.TaskType
import org.jetbrains.exposed.sql.*

class ContainerImageSynchronizationTask(
    val containerImageId: Long,
    val parameters: TaskInitializationParameters = TaskInitializationParameters(priority = taskPriority())
) : Task(parameters) {

    override val type: TaskType = ContainerImageSynchronizationTaskType

    override fun addAttributesAtStart(request: HttpServletRequest, html: TagConsumer<*>, columnSize: String, tableDetails: Boolean) {
        val coordinates = ContainerImageCoordinates.getById(containerImageId)

        html.div(columnSize) {
            html.attribute("Container Registry") {
                coordinates.registry.toLink(html, request)
            }
        }

        html.div(columnSize) {
            html.attribute("Container Repository") {
                coordinates.repository.toLink(html, request)
            }
        }

        html.div(columnSize) {
            html.attribute("Container Image") {
                coordinates.image.toLink(html, request)
            }
        }
    }

    override fun doTask() {
        val (registry, repository, image) = ContainerImageCoordinates.getById(containerImageId)

        ContainerImagesTable.lockRow(image.id) {
            val metadata = ContainerRegistryClient.getImageMetadata(registry, repository, image)
            val allTags = ContainerRegistryClient.getTags(registry, repository)

            updateProperties(image, metadata, this)
            synchronizeTags(image, metadata, allTags)
            if (metadata != null) {
                synchronizeLayers(image, metadata.manifest)
            }
        }
    }

    companion object {
        val taskPriority = DataEnumProperty("ContainerImageSynchronizationTaskPriority", TaskPriority) { TaskPriority.LOW }

        fun updateProperties(image: ContainerImage, metadata: ImageMetadata?, task: ContainerImageSynchronizationTask? = null) {
            image.lastSynchronizedAt = RuntimeClock.transactionAt
            var columnsToUpdate = mutableListOf<Column<*>>(ContainerImagesTable.lastSynchronizedAt)

            if (metadata == null) {
                if (!image.deletedFromRepository) {
                    image.deletedFromRepository = true
                    columnsToUpdate.add(ContainerImagesTable.deletedFromRepository)
                }
            } else {
                if (image.deletedFromRepository) {
                    task?.logWarn("Container image $image was previously marked as being deleted from repository but now is not.")
                    image.deletedFromRepository = false
                    columnsToUpdate.add(ContainerImagesTable.deletedFromRepository)
                }

                val lastModifiedAt = metadata.manifest.lastModifiedAt
                if (lastModifiedAt != null && image.createdAt != lastModifiedAt) {
                    image.createdAt = lastModifiedAt
                    columnsToUpdate.add(ContainerImagesTable.createdAt)
                }

                if (image.architecture == null && !metadata.architecture.isNullOrBlank()) {
                    val architecture = Architecture.mapOptionalReadableId(metadata.architecture)
                    if (architecture != null) {
                        image.architecture = architecture
                        columnsToUpdate.add(ContainerImagesTable.architecture)
                    } else {
                        task?.logWarn("Unable to map architecture: ${metadata.architecture} for image: $image")
                    }
                }

                val os = OperatingSystemType.mapOptionalReadableId(metadata.os)
                if (os != null) {
                    if (image.os != os) {
                        image.os = os
                        columnsToUpdate.add(ContainerImagesTable.os)
                    }
                } else {
                    task?.logWarn("Unable to map os: ${metadata.os} for image: $image")
                }

                if (image.osVersion.isNullOrBlank() && !metadata.osVersion.isNullOrBlank()) {
                    image.osVersion = metadata.osVersion
                    columnsToUpdate.add(ContainerImagesTable.osVersion)
                }

                val size = metadata.manifest.layers.mapNotNull { it.size }.sum()
                if (image.bytesSize != size) {
                    image.bytesSize = size
                    columnsToUpdate.add(ContainerImagesTable.bytesSize)
                }
            }

            ContainerImagesTable.update(image, columnsToUpdate)
        }

        fun synchronizeTags(image: ContainerImage, metadata: ImageMetadata?, allTags: List<TagMetadata>?) {
            val imageTags = ContainerImageTagsTable.getActiveTagsForImage(image.id)

            for (imageTag in imageTags) {
                val tagMeta = metadata?.tags?.firstOrNull { it.value == imageTag.value }
                if (tagMeta == null) {
                    val removedAt = allTags?.firstOrNull { it.value == imageTag.value }?.taggedAt ?: RuntimeClock.transactionAt
                    ContainerImageTagsTable.update({ ContainerImageTagsTable.id eq imageTag.id}) {
                        it[ContainerImageTagsTable.removedAt] = removedAt
                    }
                }
            }

            if (metadata != null) {
                for (tagMeta in metadata.tags) {
                    val imageTag = imageTags.firstOrNull { it.value == tagMeta.value }
                    if (imageTag == null) {
                        ContainerImageTagsTable.create(ContainerImageTag(image.id, tagMeta.value, tagMeta.taggedAt ?: RuntimeClock.transactionAt, null))
                    }
                }

                val name = metadata.tags.sortedWith(compareByDescending<TagMetadata> { it.taggedAt ?: 0 }.thenBy() { it.value })?.firstOrNull()?.value ?: ContainerImage.shortenDigest(
                    metadata.manifest.digest
                )
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
}