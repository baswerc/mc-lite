package com.materialcentral.io.ui

import com.materialcentral.io.Routes
import com.materialcentral.oss.ui.OssPackageUiController
import org.geezer.system.runtime.StringProperty
import com.materialcentral.user.User
import com.materialcentral.user.UserRole
import com.materialcentral.user.session.UserSession
import com.materialcentral.user.ui.UsersUiController
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.geezer.io.get
import org.geezer.io.ui.hxBoosted
import org.geezer.io.ui.hxRequest
import org.geezer.layouts.Layouts
import org.geezer.routes.*
import org.geezer.causeMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object UiRoutes {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @JvmField
    val RootUiUrlHandler: ((HttpServletRequest) -> String) = UiRoutes::getRootUiUrl

    val rootUiUrl = StringProperty("RootUiUrl", { Routes.rootUrl() })

    val routingTable: RoutingTable
        get() = _routingTable

    @JvmStatic
    val uiAdminEnabled: Boolean
        get() = _uiAdminEnabled

    private lateinit var _routingTable: RoutingTable

    private var _uiAdminEnabled = false

    fun registerRoutes(routingTable: RoutingTable) {

        routingTable.configuration.redirectProvider = object : RedirectProvider {
            override fun sendRedirect(location: String, request: HttpServletRequest, response: HttpServletResponse) {
                if (request.getHeader("Hx-Request") == "true") {
                    response.addHeader("HX-Redirect", location)
                } else {
                    response.sendRedirect(location)
                }
            }

        }

        RouteFunctionCustomParameterRegistry.registerProvider(UserSession::class) { context -> context.request.userSession }
        RouteFunctionCustomParameterRegistry.registerProvider(User::class) { context -> context.request.userSession.cachedUser }

        _routingTable = routingTable.with(rootUrlHandler = UiRoutes::getRootUiUrl, exceptionHandler = UiRoutes::handleException) {

            with("/", beforeFunction = ::requiredAuthenticated, afterFunction = ::setupLayout) {
                add(HomeUiController::get)

                with(beforeHandler = createRequireAllRoles(UserRole.VIEWER)) {

                }
            }
        }
    }

    fun getRootUiUrl(request: HttpServletRequest): String {
        return rootUiUrl()
    }

    fun handleException(e: Throwable, requestContext: RequestContext) {
        log.error("Uncaught UI error at: ${requestContext.url} (${requestContext.method}) due to: ${e.causeMessage}", e)
        throw e
    }

    fun requiredAuthenticated(context: RequestContext) {
        val userSession = context.request.optionalUserSession
        if (userSession == null) {
            throw ReturnStatus.Unauthorized401
        }
    }

    fun setupLayout(request: HttpServletRequest) {
        if (request[Layouts.LAYOUT] == null) {
            if (request.hxBoosted) {
                request.setAttribute(Layouts.LAYOUT, "boosted")
            } else if (request.hxRequest) {
                request.setAttribute(Layouts.NO_LAYOUT, "true")
            }
        }
    }

    fun requireAdministrator(request: HttpServletRequest) {
        if (!request.userSession.administrator) {
            throw ReturnStatus.Forbidden403
        }
    }

    fun createRequireAllRoles(role: UserRole, vararg additionalRoles: UserRole): (RequestContext) -> Unit {
        return { context ->
            if (!context.request.userSession.hasAllRole(role, *additionalRoles)) {
                throw ReturnStatus.Unauthorized401
            }
        }
    }

    fun createRequireAnyRoles(role: UserRole, vararg additionalRoles: UserRole): (RequestContext) -> Unit {
        return { context ->
            if (!context.request.userSession.hasAnyRole(role, *additionalRoles)) {
                throw ReturnStatus.Unauthorized401
            }
        }
    }
}