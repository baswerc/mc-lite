package com.materialcentral.container.image.download

import com.materialcentral.repository.container.registry.oci.layout.OCILayoutDirectory
import com.materialcentral.repository.container.registry.oci.manifest.ManifestLayer
import com.materialcentral.util.Tar
import com.materialcentral.util.TarExtractionDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

/**
 * A recreation of a container image file system rooted within a local directory.
 *
 * https://justdoit.tech/post/containers-made-easy-2/#downloading-and-unpacking-layers
 */
class ImageLayersDirectory {

    val rootDirectory: File

    val imageFilePathToLayerDigests: Map<String, String>

    constructor(rootDirectory: File, imageFilePathToLayerDigests: Map<String, String>) {
        this.rootDirectory = rootDirectory
        this.imageFilePathToLayerDigests = imageFilePathToLayerDigests
    }

    constructor(rootDirectory: File, ociDirectory: File) : this(rootDirectory, OCILayoutDirectory(ociDirectory))

    constructor(rootDirectory: File, ociLayoutDirectory: OCILayoutDirectory) {
        if (!rootDirectory.isDirectory) {
            if (!rootDirectory.mkdir()) {
                throw IOException("Unable to create OCI output directory: ${rootDirectory.absolutePath}")
            }
        } else if (!rootDirectory.canWrite()) {
            throw IOException("Unable to write to OCI output directory: ${rootDirectory.absolutePath}")
        }

        this.rootDirectory = rootDirectory

        val imageFilePathToLayerDigests = mutableMapOf<String, String>()
        for (entry in ociLayoutDirectory.entries) {
            for ((layer, file) in entry.layersFiles) {
                val driver = object : TarExtractionDriver {
                    override fun shouldExtractFile(filePath: String): Boolean {
                        var filePathKey = filePath
                        if (!filePathKey.startsWith("/")) {
                            filePathKey = "/$filePathKey"
                        }
                        imageFilePathToLayerDigests[filePathKey] = layer.digest

                        val index = filePath.lastIndexOf('/')
                        return if (index > 0) {
                            val directoryPath = filePath.substring(0, index)
                            val fileName = filePath.substring(index + 1)
                            if (fileName == ".wh..wh..opq") {
                                val opaqueDir = File(rootDirectory, directoryPath)
                                if (opaqueDir.isDirectory) {
                                    val children = opaqueDir.listFiles()
                                    if (children != null) {
                                        for (child in children) {
                                            if (!child.deleteRecursively()) {
                                                throw IOException("Unable to delete opaque child directory: ${child.absolutePath}")
                                            }
                                        }
                                    }
                                }

                                false
                            } else if (fileName.startsWith(".wh.")) {
                                val whiteoutFilePath = "$directoryPath/${fileName.removePrefix(".wh.")}"
                                val whiteoutFile = File(rootDirectory, whiteoutFilePath)
                                if (whiteoutFile.isFile) {
                                    if (!whiteoutFile.delete()) {
                                        throw IOException("Unable to whiteout file: ${whiteoutFile.absolutePath}")
                                    }
                                } else {
                                    log.warn("No match found for whiteout file: $filePath at: ${whiteoutFile.absolutePath} ")
                                }

                                false
                            } else {
                                true
                            }
                        } else {
                            true
                        }
                    }

                    override fun modifyLinkTargetPath(linkTargetPath: String): String {
                        var relativePath = linkTargetPath
                        if (!relativePath.startsWith("/")) {
                            relativePath = "/$relativePath"
                        }

                        return "${rootDirectory.absolutePath}$relativePath"
                    }
                }
                if (ManifestLayer.GzipMediaTypes.contains(layer.mediaType)) {
                    Tar.extractTarGz(file, rootDirectory, driver)
                } else if (ManifestLayer.ZstdMediaTypes.contains(layer.mediaType)) {
                    Tar.extractTarZtsd(file, rootDirectory, driver)
                } else if (ManifestLayer.TarMediaTypes.contains(layer.mediaType)) {
                    Tar.extractTar(file, rootDirectory, driver)
                }
            }
        }

        this.imageFilePathToLayerDigests = imageFilePathToLayerDigests.toMap()
    }

    /**
     * Get the layer digest that is responsible for the given filePath.
     */
    operator fun get(filePath: String?): String? {
        if (filePath == null) {
            return null
        }

        var filePath: String = filePath
        var digest = imageFilePathToLayerDigests[filePath]
        while (digest == null && filePath.isNotBlank()) {
            var lastIndex = filePath.lastIndexOf('/')
            if (lastIndex > 0) {
                filePath = filePath.substring(0, lastIndex)
                digest = imageFilePathToLayerDigests[filePath]
            } else {
                break
            }
        }
        return digest
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(javaClass)
    }
}
