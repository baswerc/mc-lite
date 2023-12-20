package com.materialcentral.scan.file.analysis.dependencyCheck

import arrow.core.nonEmptyListOf
import com.materialcentral.scan.FindingType
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.analysis.AnalyzerBase

abstract class DependencyCheckAnalyzer : AnalyzerBase() {
    override val toolId: String = "dependency-check"

    override val toolName: String = "Dependency-Check"

    override val url: String = "https://github.com/jeremylong/DependencyCheck"

    override val description: String = """Dependency-Check is a Software Composition Analysis (SCA) tool that attempts to detect publicly disclosed vulnerabilities contained within a project's dependencies. It does this by determining if there is a Common Platform Enumeration (CPE) identifier for a given dependency."""

    override val mediums = nonEmptyListOf(ScanMedium.ASSET)

    override val findingTypes = nonEmptyListOf(FindingType.OSS_PACKAGE_RELEASE, FindingType.KNOWN_VULNERABILITY)

    override val hasToolSpecificConfiguration: Boolean = true
}