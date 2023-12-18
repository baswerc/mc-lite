package com.materialcentral.io

import com.materialcentral.io.api.ApiRoutes
import com.materialcentral.io.ui.UiRoutes
import org.geezer.system.runtime.RequiredStringProperty
import jakarta.servlet.http.HttpServletRequest
import org.geezer.routes.RoutesConfiguration
import org.geezer.routes.RoutingEngine
import org.geezer.routes.RoutingFilter
import org.geezer.routes.RoutingTable
import org.geezer.routes.urls.UrlGen
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Routes {
    val rootUrl = RequiredStringProperty("RootUrl")

    lateinit var routesConfiguration: RoutesConfiguration

    lateinit var routingTable: RoutingTable

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun initialize(uiRoutesActive: Boolean, apiRoutesActive: Boolean) {
        if (uiRoutesActive || apiRoutesActive) {
            log.info("Initializing routes configuration.")
            routesConfiguration = RoutesConfiguration()
            routingTable = RoutingTable(routesConfiguration)

            UrlGen.initialize(Routes::getRootUrl, routingTable)

            if (uiRoutesActive) {
                UiRoutes.registerRoutes(routingTable)
            }

            if (apiRoutesActive) {
                ApiRoutes.registerRoutes(routingTable)
            }

            RoutingFilter.engine = RoutingEngine(routingTable)
        }
    }

    fun getRootUrl(request: HttpServletRequest): String {
        return rootUrl()
    }
}