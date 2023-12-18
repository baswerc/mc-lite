package com.materialcentral.tag

import org.geezer.db.cache.DataTableIdCache
import org.geezer.system.runtime.IntProperty

object TagCache : DataTableIdCache<Tag>(TagsTable) {
    override val maxRetentionSeconds: IntProperty = IntProperty("TagCacheMaxRetentionSeconds", 120)

    override val maxSize: IntProperty = IntProperty("TagCacheMaxSize", 500)
}