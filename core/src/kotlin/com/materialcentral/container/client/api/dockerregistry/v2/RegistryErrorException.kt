package com.materialcentral.container.client.api.dockerregistry.v2

import okhttp3.Response
import okio.IOException

class RegistryErrorException(val requestUrl: String, val httpCode: Int, val headers: List<Pair<String, String>>, val body: String?, val errors: List<RegistryApiError>) :
    IOException(createMessage(requestUrl, httpCode, headers, body, errors)) {

    val unauthorized: Boolean
        get() = httpCode == UnauthorizedCode

    val notFound: Boolean
        get() = httpCode == NotFoundCode

    val rateLimit: Boolean
        get() = httpCode == RateLimitCode

    val serverError: Boolean
        get() = httpCode in 500..599

    companion object {
        const val UnauthorizedCode = 401

        const val NotFoundCode = 404

        const val RateLimitCode = 429

        operator fun invoke(response: Response): RegistryErrorException {
            val requestUrl = response.request.url.toString()
            val httpCode = response.code
            val headers = response.headers.map { it }
            val body = response.body?.string()
            val errors = RegistryApiError.parse(body)

            return RegistryErrorException(requestUrl, httpCode, headers, body, errors)
        }

        fun createMessage(requestUrl: String, httpCode: Int, headers: List<Pair<String, String>>, body: String?, errors: List<RegistryApiError>): String {
            var message = "Container registry request: $requestUrl returned status: $httpCode"
            if (errors.isNotEmpty()) {
                message += " with errors: ${errors.joinToString(", ")}"
            } else if (!body.isNullOrBlank()) {
                message += " with body: $body"
            }

            return message
        }
    }
}