package com.materialcentral.container.oci.layout

import arrow.core.Either
import com.materialcentral.container.oci.configuration.ImageConfiguration
import com.materialcentral.container.oci.manifest.Manifest
import com.materialcentral.container.oci.manifest.ManifestLayer
import com.materialcentral.container.oci.manifest.v2.V2Manifest
import java.io.File
import java.io.FileReader
import java.io.IOException

class OCILayoutDirectory {

    val directory: File

    val entries: List<OCILayoutEntry>

    @Throws(IOException::class)
    constructor(directory: File) {

        this.directory = directory

        val layoutFile = File(directory, "oci-layout")
        if (!layoutFile.isFile) {
            throw IOException("No OCI layout file at: ${layoutFile.absolutePath}")
        } else if (!layoutFile.canRead()) {
            throw IOException("Cannot read OCI layout file: ${layoutFile.absolutePath}")
        }

        val ociLayout = when (val result = OCILayout.parse(FileReader(layoutFile).use { it.readText() })) {
            is Either.Left -> {
                throw IOException("Unable to parse OCI layout file: ${layoutFile.absolutePath} due to: ${result.value}")
            }

            is Either.Right -> result.value
        }

        if (ociLayout.imageLayoutVersion != OCILayout.Version) {
            throw IOException("Unsupported OCI layout version: ${ociLayout.imageLayoutVersion}")
        }

        val indexFile = File(directory, "index.json")
        if (!indexFile.isFile) {
            throw IOException("No OCI index file at: ${indexFile.absolutePath}")
        } else if (!layoutFile.canRead()) {
            throw IOException("Cannot read OCI index file: ${indexFile.absolutePath}")
        }

        val index = OCILayoutIndex.parse(FileReader(indexFile).use { it.readText() }) ?: throw IOException("Unable to parse OCI index file: ${indexFile.absolutePath}")

        val blobsDirectory = File(directory, "blobs")
        if (!blobsDirectory.isDirectory) {
            throw IOException("No OCI blobs directory at: ${blobsDirectory.absolutePath}")
        } else if (!layoutFile.canRead()) {
            throw IOException("Cannot read OCI blobs directory: ${blobsDirectory.absolutePath}")
        }

        val entries = mutableListOf<OCILayoutEntry>()

        for (indexManifest in index.manifests) {

            val manifestFile = digestToFile(blobsDirectory, indexManifest.digest)
            val manifest = Manifest.parseManifest(FileReader(manifestFile).use { it.readText() }, mediaType = indexManifest.mediaType, digest = indexManifest.digest)
                ?: throw IOException("Unable to parse manifest file: ${manifestFile.absolutePath}")

            val (configFile, config) = if (manifest is V2Manifest) {
                val configFile = digestToFile(blobsDirectory, manifest.config.digest)
                val config = when (val result = ImageConfiguration.parse(FileReader(configFile).use { it.readText() }, manifest.config.digest)) {
                    is Either.Left -> throw IOException("Unable to parse configuration file: ${configFile.absolutePath} due to: ${result.value}")
                    is Either.Right -> result.value
                }
                configFile to config
            } else {
                null to null
            }

            val layersFiles = mutableListOf<Pair<ManifestLayer, File>>()
            for (layer in manifest.layers) {
                val layerFile = digestToFile(blobsDirectory, layer.digest)
                layersFiles.add(layer to layerFile)
            }

            entries.add(OCILayoutEntry(manifest, manifestFile, config, configFile, layersFiles))
        }

        this.entries = entries
    }

    companion object {
        fun digestToFile(blobsDir: File, digest: String): File {
            val values = digest.split(':')
            if (values.size != 2) {
                throw IOException("Unable to split digest $digest into algorithm and encoded value.")
            }
            val (alg, encoded) = values[0] to values[1]
            val algDir = File(blobsDir, alg)
            if (!algDir.isDirectory) {
                throw IOException("No OCI algorithm directory at: ${algDir.absolutePath}")
            } else if (!algDir.canRead()) {
                throw IOException("Cannot read OCI algorithm directory: ${algDir.absolutePath}")
            }


            val digestFile = File(algDir, encoded)
            if (!digestFile.isFile) {
                throw IOException("No OCI digest file at: ${digestFile.absolutePath}")
            } else if (!digestFile.canRead()) {
                throw IOException("Cannot read OCI digest file: ${digestFile.absolutePath}")
            }

            return digestFile
        }
    }

}