package com.materialcentral.container.client.authentication

import com.materialcentral.repository.container.ContainerName
import com.materialcentral.container.client.api.dockerregistry.v2.BaseDockerRegistryV2ApiClient
import com.materialcentral.util.http.HttpStatusCodeException
import org.geezer.toJsonObjectOrBust
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

open class DockerHubAuthenticationProvider(val username: String? = null, val password: String? = null, val scope: String = "pull") : RegistryAuthenticationProvider {
    private val tokenCache = mutableMapOf<String, DockerHubToken>()

    private val defaultHttpClient = OkHttpClient.Builder().build()

    override fun addAuthentication(containerName: ContainerName?, request: Request.Builder, httpClient: OkHttpClient) {
        // DockerHub doesn't support global api calls (ex. GET Catalog).
        if (containerName != null) {
            val bearerToken = getBearerToken(containerName, httpClient)
            request.addHeader("Authorization", "Bearer $bearerToken")
        }
    }

    fun getBearerToken(containerName: ContainerName, httpClient: OkHttpClient = defaultHttpClient): String {
        synchronized(this) {
            for ((key, token) in tokenCache.entries) {
                if (token.expired) {
                    tokenCache.remove(key)
                }
            }
        }

        val namespaceRepository = if (containerName.namespace == null) "${BaseDockerRegistryV2ApiClient.DockerHubDefaultNamespace}/${containerName.repository}" else containerName.namespaceRepository
        val cachedToken = synchronized(this) { tokenCache[namespaceRepository] }
        if (cachedToken != null && !cachedToken.expired) {
            return cachedToken.token
        }

        val url = "https://auth.docker.io/token?service=registry.docker.io&scope=repository:$namespaceRepository:$scope"
        val builder = Request.Builder().url(url)
        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            builder.header("Authorization", Credentials.basic(username, password))
        }
        var tokenRequest = builder.build()

        return httpClient.newCall(tokenRequest).execute().use { tokenResponse ->
            if (tokenResponse.code != 200) {
                throw HttpStatusCodeException(tokenResponse)
            }

            val body = tokenResponse.body?.string() ?: throw IOException("Invalid http response ${tokenResponse.code} with no content from docker auth token request $url")
            val json = body.toJsonObjectOrBust()
            val token = json.string("token") ?: throw IOException("Invalid http response ${tokenResponse.code} with no token returned from docker auth token request $url")

            val expiresInSeconds = json.int("expires_in") ?: 0
            synchronized(this) {
                tokenCache[namespaceRepository] = DockerHubToken(token, System.currentTimeMillis() + (expiresInSeconds * 1000L))
            }

            token
        }
    }

    private class DockerHubToken(val token: String, val expiresAt: Long) {
        val expired: Boolean
            get() = System.currentTimeMillis() >= expiresAt
    }
}

object AnonymousDockerHubAuthenticationProvider : DockerHubAuthenticationProvider()