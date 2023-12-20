package com.materialcentral.scan

import org.geezer.HasName
import org.geezer.db.DataLike
import org.geezer.io.ui.Linkable

interface ScanTarget : DataLike, Linkable {
    val scanTargetType: ScanTargetType

    val scanTargetSourceType: ScanTargetSourceType
        get() = scanTargetType.scanTargetSourceType

    val scanTargetSourceId: Long

    fun getNameForScan(): String

    fun lookupScanSource(): ScanTargetSource
}