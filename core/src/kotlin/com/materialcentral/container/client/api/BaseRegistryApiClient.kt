package com.materialcentral.container.client.api

import com.materialcentral.container.registry.ContainerRegistry
import com.materialcentral.container.repository.ContainerName
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseRegistryApiClient : ContainerRegistryApiClient {

    protected val log: Logger = LoggerFactory.getLogger(javaClass)

    protected val httpClient = OkHttpClient.Builder().followRedirects(true).build()

    protected open fun toRootUrl(registry: ContainerRegistry): String {
        var url = "http"
        if (registry.ssl) {
            url += "s"
        }
        url += "://${registry.hostname}"
        return url
    }

    protected fun setupRequest(registry: ContainerRegistry, name: ContainerName?, builder: Request.Builder): Request.Builder {
        val authenticationProvider = registry.getAuthenticationProvider()
        authenticationProvider?.addAuthentication(name, builder, httpClient)
        builder.header("Host", registry.hostname)
        return builder
    }

    protected fun getResponseHeaders(response: Response): List<Pair<String, String>> {
        val headerList = mutableListOf<Pair<String, String>>()
        val headers = response.headers
        for (headerName in headers.names()) {
            headers[headerName]?.let { headerList.add(Pair(headerName, it)) }
        }
        return headerList
    }

}