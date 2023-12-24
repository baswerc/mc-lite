package com.materialcentral.container.client.authentication

import com.materialcentral.repository.container.ContainerName
import okhttp3.OkHttpClient
import okhttp3.Request

class BearerTokenAuthenticationProvider(private val bearerToken: String) : com.materialcentral.container.client.authentication.RegistryAuthenticationProvider {
    override fun addAuthentication(image: ContainerName?, request: Request.Builder, httpClient: OkHttpClient) {
        request.addHeader("Authorization", "Bearer $bearerToken")
    }
}