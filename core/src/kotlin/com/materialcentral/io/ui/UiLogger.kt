package com.materialcentral.io.ui

import jakarta.servlet.http.HttpServletRequest
import org.geezer.io.HttpRequestLogger

object UiLogger : HttpRequestLogger(UiLogger::class.java) {
    override fun convert(message: String, request: HttpServletRequest): String {
        var message = message

        val user = request.optionalUser
        if (user != null) {
            message = "u:${user.id} $message"
        }

        message = "$message (uri: ${request.requestURI})"

        return message
    }
}