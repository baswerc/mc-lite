package com.materialcentral.user

import com.materialcentral.user.ui.UsersUiController
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.HasIcon
import org.geezer.io.ui.Linkable
import org.geezer.system.runtime.RuntimeClock
import org.geezer.HasName
import org.geezer.user.AppUser
import kotlin.reflect.KFunction

class User(
    var username: String?,
    email: String,
    name: String,
    createdAt: Long,
    active: Boolean,
    lastLoginAt: Long?,
) : AppUser(email, name, createdAt, active, lastLoginAt), HasName, Linkable, HasIcon {

    constructor(email: String, name: String) : this(null, email, name, RuntimeClock.transactionAt, true,  null)

    override val route: KFunction<*> = UsersUiController::get

    override val icon: FontIcon = Icon

    companion object {
        @JvmField
        val ExternalIdentifierIcon = FontIcon("fa-id-badge", "f2c1")
    }
}