package com.materialcentral.oss

import org.geezer.db.DataEnum
import org.geezer.db.DataEnumType

enum class OssPackageApprovalState(override val id: Int, override val label: String) : DataEnum {
    NOT_SET(0, "Not Set"),
    APPROVED(1, "Approved"),
    UNAPPROVED(2, "Unapproved");

    companion object : DataEnumType<OssPackageApprovalState> {
        override val enumValues: Array<OssPackageApprovalState>  = values()
    }
}