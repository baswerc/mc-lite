package com.materialcentral.oss.host.github

import org.geezer.db.Data

class GitHubAccess(
    var apiUserName: String,
    var apiUserTokenSecretId: Long?,
    var active: Boolean
) : Data() {
}