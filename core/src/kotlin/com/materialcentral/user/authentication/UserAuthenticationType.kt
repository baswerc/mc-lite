package com.materialcentral.user.authentication

import org.geezer.db.DataEnumType
import org.geezer.user.authentication.AppUserAuthenticationType

enum class UserAuthenticationType(override val id: Int, override val label: String) : AppUserAuthenticationType {
    USERNAME_PASSWORD(0, "Username and Password")
    ;

    companion object : DataEnumType<UserAuthenticationType> {
        override val dataEnumValues: Array<UserAuthenticationType> = enumValues()

    }
}