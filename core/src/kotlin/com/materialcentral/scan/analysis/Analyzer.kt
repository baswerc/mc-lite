package com.materialcentral.scan.analysis

import arrow.core.NonEmptyList
import org.geezer.HasNameDescription
import org.geezer.io.ui.HasImage
import com.materialcentral.scan.FindingType
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.ScanTargetType
import org.geezer.HasStringId

interface Analyzer : HasStringId, HasNameDescription, HasImage {
    val url: String

    val toolId: String

    val toolName: String

    val targetType: ScanTargetType

    val mediums: NonEmptyList<ScanMedium>

    val findingTypes: NonEmptyList<FindingType>

    val hasToolSpecificConfiguration: Boolean
}