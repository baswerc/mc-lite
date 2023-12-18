package com.materialcentral.user.session

import org.geezer.system.runtime.IntProperty
import org.geezer.cache.Cache

object UserSessionsCache : Cache<String, UserSession>() {

    override val maxRetentionSeconds: IntProperty = IntProperty("UserSessionMaxRetentionSeconds", 60)

    override val maxSize: IntProperty = IntProperty("UserSessionMaxSize", 250)

    override fun loadValue(key: String): UserSession? {
        return UserSessionsTable.findByIdentifier(key)
    }
}