package com.materialcentral.oss.host.github

import com.beust.klaxon.JsonObject
import com.materialcentral.oss.host.HostRepository
import org.geezer.requiredBoolean
import org.geezer.requiredInteger
import org.geezer.requiredString
import org.geezer.requiredTimestamp

class GitHubRepository(override val name: String,
                       override val fullName: String,
                       override val description: String,
                       override val fork: Boolean,
                       override val url: String,
                       override val createdAt: Long,
                       override val updatedAt: Long,
                       override val pushedAt: Long,
                       override val homePageUrl: String,
                       override val size: Int,
                       override val stars: Int,
                       override val watchers: Int,
                       override val language: String?,
                       override val hasIssues: Boolean,
                       override val archived: Boolean,
                       override val disabled: Boolean,
                       override val openIssues: Int?,
                       override val allowForking: Boolean,
                       override val topics: List<String>,
                       val visibility: GithubProjectVisibility,
                       override val forks: Int,
                       override val defaultBranch: String,
                       override val network: Int,
                       override val subscribers: Int,
                       override val license: GitHubLicense?,
                       val contributorsUrl: String) :
    HostRepository {

    override var contributors: Int? = null

    override val public: Boolean
        get() = visibility == GithubProjectVisibility.PUBLIC

    companion object {
        const val TimestampPattern = "yyyy-MM-dd'T'hh:mm:ss'Z'"

        fun map(json: JsonObject): GitHubRepository {
            val name = json.requiredString("name")
            val fullName = json.requiredString("full_name")
            val description = json.requiredString("description")
            val fork = json.requiredBoolean("fork")
            val url = json.requiredString("url")
            val createdAt = json.requiredTimestamp("created_at", TimestampPattern)
            val updatedAt = json.requiredTimestamp("updated_at", TimestampPattern)
            val pushedAt = json.requiredTimestamp("pushed_at", TimestampPattern)
            val homePageUrl = json.requiredString("homepage")
            val size = json.requiredInteger("size")
            val stargazers = json.requiredInteger("stargazers_count")
            val watchers = json.requiredInteger("watchers_count")
            val language = json.string("language")
            val hasIssues = json.requiredBoolean("has_issues")
            val archived = json.requiredBoolean("archived")
            val disabled = json.requiredBoolean("disabled")
            val openIssues = json.requiredInteger("open_issues_count")
            val allowForking = json.requiredBoolean("allow_forking")
            val topics = json.array<String>("topics") ?: listOf()
            val visibility = GithubProjectVisibility.map(json.requiredString("visibility"))
            val forks = json.requiredInteger("forks")
            val defaultBranch = json.requiredString("default_branch")
            val network = json.requiredInteger("network_count")
            val subscribers = json.requiredInteger("subscribers_count")
            val license = GitHubLicense.map(json.obj("license"))
            val contributorsUrl = json.requiredString("contributors_url")

            return GitHubRepository(name, fullName, description, fork, url, createdAt, updatedAt, pushedAt, homePageUrl, size, stargazers, watchers, language, hasIssues, archived, disabled, openIssues, allowForking, topics, visibility, forks,
                defaultBranch, network, subscribers, license, contributorsUrl)
        }
    }
}