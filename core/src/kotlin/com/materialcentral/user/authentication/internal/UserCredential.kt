package com.materialcentral.user.authentication.internal

import org.geezer.db.Data
import org.geezer.system.runtime.IntProperty
import org.geezer.system.runtime.RuntimeClock
import org.geezer.security.SecurityFunctions

class UserCredential(val userId: Long,
                     var passwordHash: ByteArray,
                     var lastSetAt: Long) : Data() {

    override val propertiesNotLogged = listOf(::passwordHash)

    constructor(userId: Long, password: String) : this(userId, hash(password), RuntimeClock.now)

    fun updatePassword(password: String, timestamp: Long = RuntimeClock.now): UserCredential {
        passwordHash = hash(password)
        lastSetAt = timestamp
        return this
    }

    fun verify(password: String): Boolean {
        return SecurityFunctions.verify(password, passwordHash)
    }

    companion object {
        val passwordCostProperty = IntProperty("UserPasswordHashCost", 12)

        fun hash(password: String): ByteArray = SecurityFunctions.hash(password, passwordCostProperty())
    }
}