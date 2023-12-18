package com.materialcentral.container.image.scan.analysis

import arrow.core.nonEmptyListOf
import com.materialcentral.container.image.scan.ContainerImageResourceType
import com.materialcentral.scan.Scan
import com.materialcentral.scan.FindingType
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.analysis.AnalysisConfiguration
import com.materialcentral.scan.analysis.AnalyzerBase
import java.io.File

object OscapContainerImageAnalyzer : AnalyzerBase(), ContainerImageAnalyzer {
    override val toolId: String = "oscap"

    override val toolName: String = "OSCAP"

    override val mediums = nonEmptyListOf(ScanMedium.ASSET)

    override val findingTypes = nonEmptyListOf(FindingType.MISCONFIGURATION, FindingType.KNOWN_VULNERABILITY)

    override val hasToolSpecificConfiguration: Boolean = true

    override val canAnalyzeOCIDirectory: Boolean = true

    override val canAnalyzeRootFSDirectory: Boolean = true

    override val canAnalyzeCycleDX: Boolean = false

    override val canAnalyzeSpdx: Boolean = false

    override val url: String = "https://github.com/OpenSCAP/openscap"

    override val description: String = """OpenSCAP uses SCAP which is a line of specifications maintained by the NIST. SCAP was created to provide a standardized approach for maintaining system security. New specifications are governed by NIST’s SCAP Release cycle in order to provide a consistent and repeatable revision workflow."""

    override fun analyze(scan: Scan, analysisConfiguration: AnalysisConfiguration, imageResource: File, imageResourceType: ContainerImageResourceType): ContainerImageAnalysis {
        return ContainerImageAnalysis(null, null, null, analysisConfiguration, listOf())
    }

}