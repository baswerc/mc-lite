package com.materialcentral.oss

import org.geezer.HasName
import org.geezer.db.Data
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.HasIcon
import org.geezer.io.ui.Linkable
import com.materialcentral.oss.ui.OssPackageUiController
import kotlin.reflect.KFunction

class OssPackage(
    var projectId: Long?,
    val type: PackageType,
    override val name: String,
    var approvalState: OssPackageApprovalState,
    var lastSynchronizedAt: Long?
    ) : Data(), Linkable, HasName, HasIcon {

    val namespace: String?
        get() = type.splitFullName(name).first

    val packageName: String
        get() = type.splitFullName(name).second

    override val icon: FontIcon
        get() = type.icon

    override val route: KFunction<*> = OssPackageUiController::getPackage

    constructor(type: PackageType, name: String) : this(null, type, name, OssPackageApprovalState.NOT_SET, null)

    companion object {
        @JvmField
        val Icon = FontIcon("fa-box", "f466")
    }
}