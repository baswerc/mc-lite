package com.materialcentral.io.api

import com.materialcentral.io.Routes
import org.geezer.system.runtime.StringProperty
import jakarta.servlet.http.HttpServletRequest
import org.geezer.routes.RoutingTable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ApiRoutes {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    val rootApiUrl = StringProperty("RootApiUrl", {"${Routes.rootUrl()}/api"})

    fun registerRoutes(routingTable: RoutingTable) {
        log.info("Registering API routes.")


    }

    fun getRootApiUrl(request: HttpServletRequest): String {
        return rootApiUrl()
    }



}