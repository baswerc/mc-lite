package com.materialcentral.scan.filter

import com.materialcentral.scan.ScanTargetSourceType
import org.geezer.db.Data

class ScanFindingFilterOwner(
    val scanFindingFilterId: Long,
    val scanTargetSourceType: ScanTargetSourceType,
    val scanTargetSourceId: Long,
) : Data() {
}