package com.materialcentral.oss.host.github

import com.beust.klaxon.JsonObject
import com.materialcentral.oss.host.HostLicense

class GitHubLicense(override val key: String?, override val name: String?, override val spdxId: String?, override val url: String?) : HostLicense {
    companion object {
        fun map(json: JsonObject?): GitHubLicense? {
            return if (json == null) {
                return null
            } else {
                GitHubLicense(json.string("key"), json.string("name"), json.string("spdx_id"), json.string("url"))
            }
        }
    }
}