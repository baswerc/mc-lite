package com.materialcentral.scan.file.analysis

import com.materialcentral.scan.analysis.Analysis
import com.materialcentral.scan.analysis.AnalysisConfiguration
import com.materialcentral.scan.analysis.AnalysisFinding

abstract class FileAnalysis(configuration: AnalysisConfiguration, findings: List<AnalysisFinding>) : Analysis(configuration, findings) {

    val fileAnalysisFindings: List<FileAnalysisFinding>
        get() = findings as List<FileAnalysisFinding>

    val ossPackageReleases: List<OssPackageReleaseAnalysisFinding>
        get() = findings.filterIsInstance<OssPackageReleaseAnalysisFinding>()

    val knownVulnerabilities: List<KnownVulnerabilityAnalysisFinding>
        get() = findings.filterIsInstance<KnownVulnerabilityAnalysisFinding>()

    val misconfigurations: List<MisconfigurationAnalysisFinding>
        get() = findings.filterIsInstance<MisconfigurationAnalysisFinding>()

    val secrets: List<SecretAnalysisFinding>
        get() = findings.filterIsInstance<SecretAnalysisFinding>()

}