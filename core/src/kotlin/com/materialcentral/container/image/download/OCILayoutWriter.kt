package com.materialcentral.container.image.download

import com.materialcentral.repository.container.ContainerRepository
import com.materialcentral.container.image.ContainerImage
import com.materialcentral.repository.container.registry.ContainerRegistry
import com.materialcentral.repository.container.registry.client.ContainerRegistryClient
import com.materialcentral.repository.container.registry.oci.layout.IndexManifest
import com.materialcentral.repository.container.registry.oci.layout.OCILayout
import com.materialcentral.repository.container.registry.oci.layout.OCILayoutIndex
import com.materialcentral.repository.container.registry.oci.manifest.Platform
import com.materialcentral.repository.container.registry.oci.manifest.v2.DescriptorParameters
import com.materialcentral.repository.container.registry.oci.manifest.v2.V2Manifest
import java.io.*

object OCILayoutWriter {
    @Throws(IOException::class)
    fun writeTo(registry: ContainerRegistry, repository: ContainerRepository, image: ContainerImage, outputDir: File) {
        if (!outputDir.isDirectory) {
            if (!outputDir.mkdir()) {
                throw IOException("Unable to create OCI output directory: ${outputDir.absolutePath}")
            }
        } else if (!outputDir.canWrite()) {
            throw IOException("Unable to write to OCI output directory: ${outputDir.absolutePath}")
        }


        val manifest = ContainerRegistryClient.getManifest(registry, repository, image) ?: throw IOException("No image manifest found for image $image.")

        val blobsDir = File(outputDir, "blobs")
        if (!blobsDir.isDirectory) {
            if (!blobsDir.mkdir()) {
                throw IOException("Unable to create OCI blobs directory ${blobsDir.absolutePath}")
            }
        }

        val imageIndexFile = File(outputDir, "index.json")
        val indexManifests = if (imageIndexFile.exists()) {
            val imageIndex = OCILayoutIndex.parse(FileReader(imageIndexFile).use { it.readText() }) ?: throw IOException("Unable to parse existing index file ${imageIndexFile.absolutePath}")
            imageIndex.manifests.toMutableList()
        } else {
            mutableListOf()
        }

        FileWriter(digestToFile(blobsDir, manifest.digest)).use { it.write(manifest.content) }

        val configuration = if (manifest is V2Manifest) ContainerRegistryClient.getConfiguration(registry, repository, manifest.config.digest) else null

        if (configuration != null) {
            FileWriter(digestToFile(blobsDir, configuration.digest)).use { it.write(configuration.content) }
        }

        val platform = if (configuration?.architecture == null || configuration.os == null) null else Platform(configuration.architecture, configuration.os, configuration.osVersion, configuration.osFeatures, configuration.variant, listOf())
        indexManifests.add(IndexManifest(DescriptorParameters(mediaType = manifest.mediaType, digest = manifest.digest, size = manifest.content.length.toLong()), platform))

        for (layer in manifest.layers) {
            FileOutputStream(digestToFile(blobsDir, layer.digest)).use { layerOut ->
                ContainerRegistryClient.downloadLayer(registry, repository, layer, layerOut)
            }
        }

        val imageIndex = OCILayoutIndex(manifests = indexManifests)
        FileWriter(imageIndexFile).use { it.write(imageIndex.toJsonString()) }

        val ociLayoutFile = File(outputDir, "oci-layout")
        if (!ociLayoutFile.isFile) {
            FileWriter(ociLayoutFile).use { it.write(OCILayout().toJsonString()) }
        }
    }

    fun digestToFile(blobsDir: File, digest: String): File {
        val values = digest.split(':')
        if (values.size != 2) {
            throw IOException("Unable to split digest $digest into algorithm and encoded value.")
        }
        val (alg, encoded) = values[0] to values[1]
        val algDir = File(blobsDir, alg)
        if (!algDir.isDirectory) {
            if (!algDir.mkdir()) {
                throw IOException("Unable to create OCI algorithm directory ${algDir.absolutePath}")
            }
        }

        val encodedFile = File(algDir, encoded)
        if (encodedFile.exists()) {
            throw IOException("The manifest layer file ${encodedFile.absolutePath} already exists.")
        }

        return encodedFile
    }
}