package com.materialcentral.container.image.scan.analysis

import com.materialcentral.container.image.scan.ContainerImageResourceType
import com.materialcentral.scan.Scan
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.ScanTargetType
import com.materialcentral.scan.analysis.AnalysisConfiguration
import com.materialcentral.scan.file.analysis.grype.GrypeAnalyzer
import com.materialcentral.scan.file.analysis.syft.SyftAnalyzer
import com.materialcentral.scan.file.analysis.truffleHog.TruffleHogAnalyzer
import java.io.File

object TruffleHogContainerImageAnalyzer : TruffleHogAnalyzer(), ContainerImageAnalyzer {
    override val canAnalyzeOCIDirectory: Boolean = true

    override val canAnalyzeRootFSDirectory: Boolean = true

    override val canAnalyzeCycleDX: Boolean = true

    override val canAnalyzeSpdx: Boolean = true

    override fun analyze(scan: Scan, analysisConfiguration: AnalysisConfiguration, imageResource: File, imageResourceType: ContainerImageResourceType): ContainerImageAnalysis {
        return ContainerImageAnalysis(null, null, null, analysisConfiguration, listOf())
    }
}