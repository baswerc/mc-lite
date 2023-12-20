package com.materialcentral

import org.geezer.db.DataEnum
import org.geezer.db.DataEnumType

enum class CandidateMatchType(override val id: Int, override val label: String) : DataEnum {
    ALL(0, "All"),
    MATCHED_TAGS(1, "Matched Tags"),
    ASSIGNED(2, "Assigned");

    companion object : DataEnumType<CandidateMatchType> {
        override val dataEnumValues: Array<CandidateMatchType> = values()
    }
}