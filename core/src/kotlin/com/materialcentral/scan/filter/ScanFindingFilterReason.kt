package com.materialcentral.scan.filter

import org.geezer.HasReadableId
import org.geezer.db.DataEnum
import org.geezer.db.DataEnumType

enum class ScanFindingFilterReason(override val id: Int, override val readableId: String, override val label: String, val description: String) : DataEnum, HasReadableId {
    FALSE_POSITIVE(5, "false-positive", "False Positive", "The scan item does not exists in the target. The analyzer incorrectly identified it."),
    NO_AFFECT(1, "no-affect", "No Affect", "The scan item exists in the target but has no affect. For materials this would indicate the material is not used. For vulnerabilities this would indicate the vulnerability cannot be exploited."),
    WONT_FIX(2, "wont-fix", "Won't Fix", "The owners of the source of this finding have no intention of fixing it."),
    END_OF_LIFE(3, "end-of-life", "End Of Life", "The target of this finding is no longer being actively maintained."),
    UNDER_INVESTIGATION(4, "under-investigation", "Under Investigation", "The finding is being investigated. Until it is confirmed it should not be treated as legitimate."),
    WILL_NOT_FIX(5, "will-not-fix", "Will Not Fix", "The scan item exists in the target but it will not be fixed. More details may be provided in the filter description."),
    ;

    companion object : DataEnumType<ScanFindingFilterReason> {
        override val dataEnumValues: Array<ScanFindingFilterReason> = values()
    }
}