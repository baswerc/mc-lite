package com.materialcentral.scan.file

import com.materialcentral.scan.ScanFinding
import com.materialcentral.scan.FindingSeverity

abstract class FileScanFinding(
    val inheritedFinding: Boolean, // ex. finding from a container image's base image layer
    scanId: Long,
    analyzerIds: List<String>,
    analyzerSeverities: Map<String, FindingSeverity>,
    filterId: Long?,
) : ScanFinding(scanId, analyzerIds, analyzerSeverities, filterId) {

    abstract val filePathId: Long?

    override val locationId: Long?
        get() = filePathId
}