package com.materialcentral.scan.file.analysis.grype

import arrow.core.nonEmptyListOf
import com.materialcentral.scan.FindingType
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.analysis.AnalyzerBase

abstract class GrypeAnalyzer : AnalyzerBase() {
    override val toolId: String = "grype"

    override val toolName: String = "Grype"

    override val url: String = "https://github.com/anchore/grype"

    override val description: String = """An easy-to-integrate open source vulnerability scanning tool for container images and filesystems."""

    override val mediums = nonEmptyListOf(ScanMedium.METADATA, ScanMedium.ASSET)

    override val findingTypes = nonEmptyListOf(FindingType.KNOWN_VULNERABILITY)

    override val hasToolSpecificConfiguration: Boolean = true
}