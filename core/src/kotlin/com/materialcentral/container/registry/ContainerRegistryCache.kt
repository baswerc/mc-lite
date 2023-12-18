package com.materialcentral.container.registry

import org.geezer.db.cache.FullTableCache

object ContainerRegistryCache : FullTableCache<ContainerRegistry>(ContainerRegistriesTable) {

}