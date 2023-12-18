package com.materialcentral.oss.host

import org.geezer.db.DataEnum
import org.geezer.db.DataEnumType
import org.geezer.io.ui.FontIcon

enum class OpenSourceProjectHost(override val id: Int, override val label: String, val hostname: String, val icon: FontIcon) : DataEnum {
    GITHUB(0, "GitHub", "github.com", FontIcon("fa-github", "f09b", true)),
    GITLAB(1, "GitLab", "gitlab.com", FontIcon("fa-gitlab", "f296", true)),
    BITBUCKET(2, "BitBucket", "bitbucket.org", FontIcon("fa-bitbucket", "f171", true));

    companion object : DataEnumType<OpenSourceProjectHost> {
        override val enumValues: Array<OpenSourceProjectHost> = values()

        fun mapHostName(hostname: String): OpenSourceProjectHost? = values().firstOrNull { it.hostname.equals(hostname, true) }
    }
}