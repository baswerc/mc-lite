package com.materialcentral.container.image.scan

import com.materialcentral.os.OperatingSystemType
import com.materialcentral.repository.container.ContainerRepositoryCoordinates
import com.materialcentral.container.image.ContainerImage
import com.materialcentral.container.image.ContainerImageCoordinates
import com.materialcentral.container.image.ContainerImagesTable
import com.materialcentral.container.image.download.ImageDownloadClient
import com.materialcentral.container.image.download.ImageLayersDirectory
import com.materialcentral.container.image.sbom.ContainerImageCycloneDxExporter
import com.materialcentral.container.image.sbom.ContainerImageSpdxExporter
import com.materialcentral.container.image.scan.analysis.ContainerImageAnalysis
import com.materialcentral.container.image.scan.analysis.ContainerImageAnalyzer
import com.materialcentral.scan.Scan
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.Scanner
import com.materialcentral.scan.analysis.Analysis
import com.materialcentral.scan.analysis.AnalysisConfiguration
import com.materialcentral.scan.analysis.Analyzer
import org.geezer.io.IO
import java.io.File

object ContainerImageScanner : Scanner<ContainerImage> {
    override fun scan(scan: Scan, image: ContainerImage, analyzersConfigurations: List<Pair<Analyzer, AnalysisConfiguration>>): List<Analysis> {

        val repositoryCoordinates = ContainerRepositoryCoordinates.findById(image.containerRepositoryId)
        if (repositoryCoordinates == null) {
            throw scan.failed("Unable to find container repository with id: ${image.containerRepositoryId}")
        }

        val (registry, repository) = repositoryCoordinates

        if (!repository.active) {
            throw scan.failed("Container repository ${repository.name} is not active.")
        }

        if (!registry.active) {
            throw scan.failed("Container registry ${registry.name} is not active.")
        }

        val coordinates = ContainerImageCoordinates(image, repository, registry)

        val containerImageAnalyzers = mutableListOf<Pair<ContainerImageAnalyzer, AnalysisConfiguration>>()
        for ((analyzer, configuration) in analyzersConfigurations) {
            if (analyzer is ContainerImageAnalyzer) {
                containerImageAnalyzers.add(analyzer to configuration)
            } else {
                throw scan.failed("Analyzer ${analyzer.javaClass.kotlin.qualifiedName} does not implement ${ContainerImageAnalyzer::class.simpleName} interface.")
            }
        }

        if (containerImageAnalyzers.isEmpty()) {
            throw scan.failed("No analyzers implemented ${ContainerImageAnalyzer::class.simpleName} interface.")
        }

        return IO.tempDirBarrier {
            val analyses = when (scan.medium) {
                ScanMedium.ASSET -> {
                    scanAsset(scan, coordinates, containerImageAnalyzers)
                }

                ScanMedium.METADATA -> {
                    scanMetadata(scan, coordinates, containerImageAnalyzers)
                }

                else -> {
                    throw scan.failed("Unsupported scan medium ${scan.medium.label}.")
                }
            }

            if (analyses.isEmpty()) {
                throw scan.failed("No analyzers were able")
            } else {
                val layers = coordinates.layers
                for (analysis in analyses) {
                    for (repositoryFinding in analysis.fileAnalysisFindings) {
                        repositoryFinding.layerId?.let { layerId ->
                            repositoryFinding.inheritedFinding = layers.firstOrNull { it.digest == layerId }?.parentLayer == true
                        }
                    }
                }

                if (image.os == null) {
                    image.os = analyses.firstNotNullOfOrNull { it.os }
                }

                if (image.os == null || image.os == OperatingSystemType.LINUX && image.linuxDistribution == null) {
                    image.linuxDistribution = analyses.firstNotNullOfOrNull { it.linuxDistribution }
                    if (image.os == null && image.linuxDistribution != null) {
                        image.os = OperatingSystemType.LINUX
                    }
                }

                if (image.architecture == null) {
                    image.architecture = analyses.firstNotNullOfOrNull { it.architecture }
                }

                ContainerImagesTable.update(image, ContainerImagesTable.os, ContainerImagesTable.linuxDistribution, ContainerImagesTable.architecture)

                analyses
            }
        }

    }

    fun scanMetadata(scan: Scan, coordinates: ContainerImageCoordinates, analyzersConfigurations: List<Pair<ContainerImageAnalyzer, AnalysisConfiguration>>): List<ContainerImageAnalysis> {
        var cycloneDxFile: File? = null
        var spdxFile: File? = null

        val analyses = mutableListOf<ContainerImageAnalysis>()
        for ((analyzer, configuration) in analyzersConfigurations) {

            if (analyzer.canAnalyzeCycleDX) {
                if (cycloneDxFile == null) {
                    cycloneDxFile = IO.createTempFile("json", "cyclone")
                    ContainerImageCycloneDxExporter.exportJson(coordinates, cycloneDxFile)
                }

                analyses.add(analyzer.analyze(scan, configuration, cycloneDxFile, ContainerImageResourceType.CYCLONE_DX_FILE))
            } else if (analyzer.canAnalyzeSpdx) {
                if (spdxFile == null) {
                    spdxFile = IO.createTempFile("json", "spdx")
                    ContainerImageSpdxExporter.exportJson(coordinates, spdxFile)
                }

                analyses.add(analyzer.analyze(scan, configuration, spdxFile, ContainerImageResourceType.SPDX_FILE))
            } else {
                throw scan.failed("${analyzer.id} cannot analyze an image SBOM.")
            }
        }

        return analyses
    }

    fun scanAsset(scan: Scan, coordinates: ContainerImageCoordinates, analyzersConfigurations: List<Pair<ContainerImageAnalyzer, AnalysisConfiguration>>): List<ContainerImageAnalysis> {
        var ociDir: File = IO.createTempDir()
        ImageDownloadClient.copyToOciDir(coordinates, ociDir)

        var rootFsDir: File? = null
        var imageLayersDirectory: ImageLayersDirectory? = null

        val analyses = mutableListOf<ContainerImageAnalysis>()
        for ((analyzer, configuration) in analyzersConfigurations) {

            if (analyzer.canAnalyzeOCIDirectory) {
                analyses.add(analyzer.analyze(scan, configuration, ociDir, ContainerImageResourceType.OCI_DIR))
            } else if (analyzer.canAnalyzeRootFSDirectory) {
                if (imageLayersDirectory == null || rootFsDir == null) {
                    rootFsDir = IO.createTempDir()
                    imageLayersDirectory = ImageLayersDirectory(rootFsDir, ociDir)
                }

                val analysis = analyzer.analyze(scan, configuration, rootFsDir, ContainerImageResourceType.ROOT_FS_DIR)

                val rootFsDirPath = rootFsDir.absolutePath
                for (repositoryFinding in analysis.fileAnalysisFindings) {
                    repositoryFinding.layerId = imageLayersDirectory[repositoryFinding.filePath]
                    repositoryFinding.removeFilePathPrefix(rootFsDirPath)
                }

                analyses.add(analysis)
            } else {
                throw scan.failed("${analyzer.name} cannot analyze image material median.")
            }
        }

        return analyses

    }
}