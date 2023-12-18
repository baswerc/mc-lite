package com.materialcentral

import org.geezer.db.cache.DataTableIdCache
import org.geezer.system.runtime.IntProperty

object LocationsCache : DataTableIdCache<Location>(LocationsTable) {
    override val maxRetentionSeconds: IntProperty = IntProperty("LocationMaxCachedSeconds", 60)

    override val maxSize: IntProperty = IntProperty("LocationMaxCacheSize", 1000)
}