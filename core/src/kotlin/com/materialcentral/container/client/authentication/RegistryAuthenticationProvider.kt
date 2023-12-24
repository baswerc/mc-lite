package com.materialcentral.container.client.authentication

import com.materialcentral.repository.container.ContainerName
import okhttp3.OkHttpClient
import okhttp3.Request

interface RegistryAuthenticationProvider {
    fun addAuthentication(image: ContainerName?, request: Request.Builder, httpClient: OkHttpClient)
}