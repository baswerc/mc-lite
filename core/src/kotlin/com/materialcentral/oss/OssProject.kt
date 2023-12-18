package com.materialcentral.oss

import org.geezer.HasNameDescription
import org.geezer.db.Data
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.HasIcon
import org.geezer.io.ui.Linkable
import com.materialcentral.oss.host.OpenSourceProjectHost
import kotlin.reflect.KFunction

class OssProject(
    var host: OpenSourceProjectHost,
    var organization: String,
    var repository: String,
    override var description: String?,
    var license: OSSLicense?,
    var public: Boolean?,
    var stars: Int?,
    var subscribers: Int?,
    var watchers: Int?,
    var forks : Int?,
    var contributors : Int?,
    var openIssues: Int?,
    var archived: Boolean?,
    var disabled: Boolean?,
    var createdAt: Long?,
    var lastCommitAt: Long?,
) : Data(), Linkable, HasNameDescription, HasIcon {

    override val route: KFunction<*>
        get() = TODO("Not yet implemented")

    override val icon: FontIcon = Icon

    override val name: String
        get() = "$organization/$repository"

    companion object {
        @JvmField
        val Icon = FontIcon("fa-gear-code", "e5e8")
    }
}