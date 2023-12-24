package com.materialcentral.container.client.authentication

import com.materialcentral.repository.container.ContainerName
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request

class BasicAuthenticationProvider(private val username: String, private val password: String) : com.materialcentral.container.client.authentication.RegistryAuthenticationProvider {
    override fun addAuthentication(image: ContainerName?, request: Request.Builder, httpClient: OkHttpClient) {
        request.addHeader("Authorization", Credentials.basic(username, password))
    }
}