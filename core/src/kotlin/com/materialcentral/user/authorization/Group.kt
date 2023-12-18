package com.materialcentral.user.authorization

import com.materialcentral.user.UserRole
import com.materialcentral.user.authorization.ui.GroupsUiController
import org.geezer.db.Data
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.HasNameDescriptionIcon
import org.geezer.io.ui.Linkable
import kotlin.reflect.KFunction

class Group(
    override var name: String,
    override var description: String?,
    var memberRoles: List<UserRole>,
    var active: Boolean,
    val createdAt: Long

) : Data(), HasNameDescriptionIcon, Linkable {
    override val icon: FontIcon = Icon

    override val route: KFunction<*> = GroupsUiController::getGroup

    companion object {
        @JvmField
        val Icon = FontIcon("fa-people-group", "e533")
    }

}