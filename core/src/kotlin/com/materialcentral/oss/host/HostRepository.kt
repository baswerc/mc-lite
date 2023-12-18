package com.materialcentral.oss.host

interface HostRepository {
    val name: String
    val fullName: String
    val description: String
    val fork: Boolean
    val url: String
    val createdAt: Long
    val updatedAt: Long
    val pushedAt: Long
    val homePageUrl: String
    val contributors: Int?
    val size: Int
    val stars: Int
    val watchers: Int
    val language: String?
    val hasIssues: Boolean
    val archived: Boolean
    val disabled: Boolean
    val openIssues: Int?
    val allowForking: Boolean
    val topics: List<String>
    val public: Boolean
    val forks: Int
    val defaultBranch: String
    val network: Int
    val subscribers: Int
    val license: HostLicense?
}