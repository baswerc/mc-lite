package com.materialcentral.oss.host.github

import com.beust.klaxon.JsonObject
import com.materialcentral.oss.host.OpenSourceProjectHost
import com.materialcentral.oss.OssProject
import com.materialcentral.oss.host.HostRepository
import com.materialcentral.secret.SecretsCache
import org.geezer.toJsonObjectOrBust
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.geezer.toJsonArrayOrBust
import java.io.IOException

object GithubRestClient {
    val client = OkHttpClient.Builder().followRedirects(true).build()

    var rateLimitRemaining: Int? = null
        private set

    var rateLimitReset: Long? = null
        private set

    val canMakeRequest: Boolean
        get() = rateLimitRemaining.let { it == null || it > 1 }

    const val ApiRootUrl = "https://api.github.com"

    @Synchronized
    fun getHostRepository(project: OssProject): HostRepository? {
        require(project.host == OpenSourceProjectHost.GITHUB) { "Project $project cannot be synchronized with GitHub due to invalid host ${project.host}."}
        val organization = project.organization
        require(!organization.isNullOrBlank()) { "GitHub project $project cannot be synchronized due to no organization."}
        val repository = project.repository
        require(!repository.isNullOrBlank()) { "GitHub project $project cannot be synchronized due to no repository."}

        check(canMakeRequest) { "GitHub project $project cannot be synchronized."}

        val request = setupRequest(Request.Builder().url("$ApiRootUrl/repos/$organization/$repository").addHeader("Accept", "application/vnd.github+json"))
        return client.newCall(request).execute().use { response ->
            onResponse(response)

            when (response.code) {
                200 -> {
                    val repository = GitHubRepository.map(response.body!!.string().toJsonObjectOrBust())

                    client.newCall(setupRequest(Request.Builder().url(repository.contributorsUrl).addHeader("Accept", "application/vnd.github+json"))).execute().use { response ->
                        when (response.code) {
                            200 -> repository.contributors = response.body!!.string().toJsonArrayOrBust<JsonObject>().size
                            else -> throw IOException("GitHub return backed status code ${response.code} for contributors request ${repository.contributorsUrl}")
                        }
                    }

                    repository
                }
                301, 404 -> null
                else -> throw IOException("GitHub returned back status code ${response.code} for request ${request.url}")
            }
        }
    }

    private fun setupRequest(requestBuilder: Request.Builder): Request {
        val gitHubAccess = GitHubAccessesTableCache.getRandom()
        val githubApiUsername = gitHubAccess?.apiUserName
        val githubApiToken = SecretsCache.find(gitHubAccess?.apiUserTokenSecretId)?.getValue()

        if (!githubApiUsername.isNullOrBlank() && !githubApiToken.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Basic ${Credentials.basic(githubApiUsername, githubApiToken)}")
        }
        return requestBuilder.build()
    }

    private fun onResponse(response: Response): Response {
        response.header("x-ratelimit-remaining")?.toIntOrNull()?.let { rateLimitRemaining = it }
        response.header("x-ratelimit-reset")?.toLongOrNull()?.let { rateLimitReset = it }
        return response
    }
}