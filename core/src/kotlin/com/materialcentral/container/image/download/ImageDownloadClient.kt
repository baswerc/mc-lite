package com.materialcentral.container.image.download

import com.beust.klaxon.JsonObject
import com.materialcentral.container.image.ContainerImageCoordinates
import org.geezer.io.IO
import org.geezer.toJsonObjectOrBust
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.file.Files

object ImageDownloadClient {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    fun copyToOciDir(imageCoordinates: ContainerImageCoordinates, toDir: File) {
        SkopeoClient.copy(imageCoordinates, "oci", toDir)
    }

    fun copyToRootFsDir(imageCoordinates: ContainerImageCoordinates, toDir: File) {
        log.info("Copying image ${imageCoordinates.name} to ${toDir.absolutePath}")
        IO.withTempDir { copyDir ->
            SkopeoClient.copy(imageCoordinates, "dir", copyDir)
            val manifestFile = File(copyDir, "manifest.json")

            if (!manifestFile.isFile) {
                throw IOException("Manifest file ${manifestFile.absolutePath} does not exists after copy.")
            }

            FileReader(manifestFile).use { reader ->
                val json = reader.readText().toJsonObjectOrBust()
                val layers = json.array<JsonObject>("layers")
                if (layers.isNullOrEmpty()) {
                    throw IllegalArgumentException("Image manifest JSON has no layers.")
                }

                for (layer in layers) {
                    layer.string("mediaType")?.let { mediaType ->
                        if (mediaType.endsWith("tar.gzip")) {
                            layer.string("digest")?.let { digest ->
                                val digestFile = File(copyDir, digest.replace("sha256:", ""))
                                if (digestFile.isFile) {
                                    val tarCmd = listOf("tar", "-xf", digestFile.absolutePath, "-C", toDir.absolutePath)
                                    val tarProcessStatus = ProcessBuilder().command(tarCmd).inheritIO().start().waitFor()
                                    if (tarProcessStatus != 0) {
                                        throw IOException("tar process: ${tarCmd.joinToString(" ")} returned status $tarProcessStatus")
                                    }

                                    val chmodCmd = listOf("chmod", "-R", "ugo+rwX", toDir.absolutePath)
                                    val chmodProcessStatus = ProcessBuilder().command(chmodCmd).inheritIO().start().waitFor()
                                    if (chmodProcessStatus != 0) {
                                        throw IOException("chmod process: ${chmodCmd.joinToString(" ")} returned status $chmodProcessStatus")
                                    }
                                } else {
                                    throw IOException("Digest file ${digestFile.absolutePath} does not exists.")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Copies each layer of the given image to a separate directory.
     * @return A list of layer digest to downloaded directory.
     */
    fun copyToLayerDirs(imageCoordinates: ContainerImageCoordinates): List<Pair<String, File>> {
        return IO.withTempDir { copyDir ->
            SkopeoClient.copy(imageCoordinates, "dir", copyDir)
            val manifestFile = File(copyDir, "manifest.json")

            if (!manifestFile.isFile) {
                throw IOException("Manifest file ${manifestFile.absolutePath} does not exist after copy.")
            }

            val layersRootDir = Files.createTempDirectory("layers-dir").toFile()
            layersRootDir.deleteOnExit()

            val layerDirs = mutableListOf<Pair<String, File>>()

            FileReader(manifestFile).use { reader ->
                val json = reader.readText().toJsonObjectOrBust()
                val layers = json.array<JsonObject>("layers")
                if (layers.isNullOrEmpty()) {
                    throw IllegalArgumentException("Image manifest JSON has no layers.")
                }

                for (layer in layers) {
                    layer.string("mediaType")?.let { mediaType ->
                        if (mediaType.endsWith("tar.gzip")) {
                            layer.string("digest")?.let { digest ->
                                val digestFile = File(copyDir, digest.replace("sha256:", ""))
                                if (digestFile.isFile) {
                                    val digestDir = File(layersRootDir, digest)
                                    digestDir.mkdir()

                                    val tarCmd = listOf("tar", "-xf", digestFile.absolutePath, "-C", digestDir.absolutePath)
                                    val tarProcessStatus = ProcessBuilder().command(tarCmd).inheritIO().start().waitFor()
                                    if (tarProcessStatus != 0) {
                                        throw IOException("tar process: ${tarCmd.joinToString(" ")} returned status $tarProcessStatus")
                                    }

                                    val chmodCmd = listOf("chmod", "-R", "ugo+rwX", digestDir.absolutePath)
                                    val chmodProcessStatus = ProcessBuilder().command(chmodCmd).inheritIO().start().waitFor()
                                    if (chmodProcessStatus != 0) {
                                        throw IOException("chmod process: ${chmodCmd.joinToString(" ")} returned status $chmodProcessStatus")
                                    }

                                    layerDirs.add(digest to digestDir)
                                } else {
                                    throw IOException("Digest file ${digestFile.absolutePath} does not exists.")
                                }
                            }
                        }
                    }
                }
            }

            layerDirs
        }
    }
}