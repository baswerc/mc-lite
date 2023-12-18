package com.materialcentral.container.image.scan.analysis

import com.materialcentral.container.image.scan.ContainerImageResourceType
import com.materialcentral.scan.Scan
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.ScanTargetType
import com.materialcentral.scan.analysis.AnalysisConfiguration
import com.materialcentral.scan.analysis.Analyzer
import java.io.File

interface ContainerImageAnalyzer : Analyzer {
    override val targetType: ScanTargetType
        get() = ScanTargetType.CONTAINER_IMAGE

    val canAnalyzeOCIDirectory: Boolean

    val canAnalyzeRootFSDirectory: Boolean

    val canAnalyzeCycleDX: Boolean

    val canAnalyzeSpdx: Boolean

    fun analyze(scan: Scan, analysisConfiguration: AnalysisConfiguration, imageResource: File, imageResourceType: ContainerImageResourceType): ContainerImageAnalysis
}