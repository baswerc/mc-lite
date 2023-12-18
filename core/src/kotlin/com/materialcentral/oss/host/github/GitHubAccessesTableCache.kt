package com.materialcentral.oss.host.github

import org.geezer.db.cache.FullTableCache
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object GitHubAccessesTableCache : FullTableCache<GitHubAccess>(GitHubAccessesTable, filterOp = (GitHubAccessesTable.active eq true)) {
}