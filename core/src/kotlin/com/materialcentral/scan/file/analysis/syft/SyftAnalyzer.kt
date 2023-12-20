package com.materialcentral.scan.file.analysis.syft

import arrow.core.nonEmptyListOf
import com.materialcentral.scan.FindingType
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.analysis.AnalyzerBase

abstract class SyftAnalyzer : AnalyzerBase() {
    override val toolId: String = "syft"

    override val toolName: String = "Syft"

    override val url: String = "https://github.com/anchore/syft"

    override val description: String = """A CLI tool for generating a Software Bill of Materials (SBOM) from container images and filesystems."""

    override val mediums = nonEmptyListOf(ScanMedium.METADATA, ScanMedium.ASSET)

    override val findingTypes = nonEmptyListOf(FindingType.OSS_PACKAGE_RELEASE)

    override val hasToolSpecificConfiguration: Boolean = true
}