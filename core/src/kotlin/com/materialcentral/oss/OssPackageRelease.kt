package com.materialcentral.oss

import org.geezer.HasName
import org.geezer.db.Data
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.HasIcon
import org.geezer.io.ui.Linkable
import com.materialcentral.oss.ui.OssPackageUiController
import com.materialcentral.version.SemanticVersion
import kotlin.reflect.KFunction

class OssPackageRelease(
    val ossPackageId: Long,
    val version: String,
    val majorVersion: Long?,
    val minorVersion: Long?,
    val patchVersion: Long?,
    var sizeBytes: Int?,
    var md5DigestId: Long?,
    var sha1DigestId: Long?,
    var sha256DigestId: Long?,
    var createdAt: Long?
) : Data(), Linkable, HasName, HasIcon {

    override val name: String
        get() = version

    override val icon: FontIcon = Icon

    override val route: KFunction<*> = OssPackageUiController::getRelease

    companion object {

        @JvmField
        val Icon = FontIcon("fa-file-zipper", "f1c6")

        operator fun invoke(ossPackageId: Long, version: String, createdAt: Long? = null): OssPackageRelease {
            val versionSegments = SemanticVersion.parseDigitsAndQualifiers(version)
            return OssPackageRelease(ossPackageId, version, versionSegments.getOrNull(0)?.first, versionSegments.getOrNull(1)?.first, versionSegments.getOrNull(2)?.first,
                null, null, null, null, createdAt)
        }
    }
}