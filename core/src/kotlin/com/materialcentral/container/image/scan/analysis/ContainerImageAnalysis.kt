package com.materialcentral.container.image.scan.analysis

import com.materialcentral.os.Architecture
import com.materialcentral.os.LinuxDistribution
import com.materialcentral.os.OperatingSystemType
import com.materialcentral.scan.analysis.AnalysisConfiguration
import com.materialcentral.scan.analysis.AnalysisFinding
import com.materialcentral.scan.file.analysis.FileAnalysis

class ContainerImageAnalysis(
    var os: OperatingSystemType? = null,
    var linuxDistribution: LinuxDistribution? = null,
    var architecture: Architecture? = null,
    configuration: AnalysisConfiguration,
    findings: List<AnalysisFinding>
) : FileAnalysis(configuration, findings) {
}