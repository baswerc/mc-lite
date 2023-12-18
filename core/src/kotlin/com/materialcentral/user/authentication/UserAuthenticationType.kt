package com.materialcentral.user.authentication

import org.geezer.db.DataEnum
import org.geezer.db.DataEnumType

enum class UserAuthenticationType(override val id: Int, override val label: String) : DataEnum {
    INTERNAL(0, "Internal"),
    LDAP(1, "LDAP")
    ;

    companion object : DataEnumType<UserAuthenticationType> {
        override val dataEnumValues: Array<UserAuthenticationType> = enumValues()

    }
}