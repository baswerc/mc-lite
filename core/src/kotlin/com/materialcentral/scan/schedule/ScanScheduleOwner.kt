package com.materialcentral.scan.schedule

import com.materialcentral.scan.ScanTargetSourceType
import org.geezer.db.Data

class ScanScheduleOwner(
    val scanScheduleId: Long,
    val scanTargetSourceType: ScanTargetSourceType,
    val scanTargetSourceId: Long,
) : Data() {
}