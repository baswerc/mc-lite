package com.materialcentral.io.ui

import com.materialcentral.io.Routes
import com.materialcentral.oss.ui.OssPackageUiController
import org.geezer.system.runtime.StringProperty
import com.materialcentral.user.User
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
                add(HomeRoutes::get)

                with(beforeHandler = createRoleGuard(Role.VIEWER)) {

                    with("/organizations") {
                        add("/{}", OrganizationUiController::get)
                    }


                    with ("/oss") {
                        with("/packages") {
                            add("/{}", OssPackageUiController::getPackage)
                            add("/releases/{}", OssPackageUiController::getRelease)
                        }

                        with("/projects") {
                            add("/{}", OssProjectUiController::getProject)
                        }
                    }


                    with("/known-vulnerabilities") {
                        add(KnownVulnerabilityUiController::get)
                        add("/{}", KnownVulnerabilityUiController::getKnownVulnerability)
                    }

                    with("/container-repositories") {

                        add(ContainerRepositoryUiController::getAll)
                        add("/row-details/{}", ContainerRepositoryUiController::getAllTableDetail)

                        add("/{}", ContainerRepositoryUiController::getRepository)
                        add("/{}/images", ContainerRepositoryUiController::getImages)
                        add("/{}/images/badge", ContainerRepositoryUiController::getImagesBadge)
                        add("/{}/images/table", ContainerRepositoryUiController::getImagesTable)
                        add("/{}/policies", ContainerRepositoryUiController::getPolicies)
                        add("/{}/policies/badge", ContainerRepositoryUiController::getPoliciesBadge)
                        add("/{}/violations", ContainerRepositoryUiController::getViolations)
                        add("/{}/violations/badge", ContainerRepositoryUiController::getViolationsBadge)

                        add("/{}/edit", ContainerRepositoryUiController::getEdit, ContainerRepositoryUiController::postUpdate)
                        add("/new", ContainerRepositoryUiController::getNew, ContainerRepositoryUiController::postCreate)

                        add("/{}/synchronize-images", ContainerRepositoryUiController::postSynchronizeImages)
                        add("/{}/evaluate-policies", ContainerRepositoryUiController::postEvaluatePolicies)

                        with("/images") {
                            add("{}/row-details", ContainerImageUiController::getImageRowDetails)

                            add("{}", ContainerImageUiController::getImage)
                            add("{}/tags", ContainerImageUiController::getTags)
                            add("{}/tags/badge", ContainerImageUiController::getTagsBadge)
                            add("{}/layers", ContainerImageUiController::getLayers)
                            add("{}/layers/badge", ContainerImageUiController::getLayersBadge)
                            add("{}/schedules", ContainerImageUiController::getScanSchedules)
                            add("/{}/schedules/row-details/{}", ContainerImageUiController::getScanScheduleDetails)
                            add("/{}/schedules/{}/start", ContainerImageUiController::postStartScheduleScan)
                            add("{}/comments", ContainerImageUiController::getComments)
                            add("{}/comments/badge", ContainerImageUiController::getCommentsBadge)
                            add("{}/comments/list", ContainerImageUiController::getCommentsList)

                            add("{}/comments/update", ContainerImageUiController::postUpdateComment)
                            add("{}/comments/create", ContainerImageUiController::postCreateComment)
                            add("{}/comments/delete", ContainerImageUiController::postDeleteComment)

                            add("{}/synchronize", ContainerImageUiController::postSynchronize)
                            add("{}/scan", ContainerImageUiController::getStartScan)
                        }

                        with("/registries", beforeFunction = ContainerRegistryUiController::requireViewRepositoryPermission) {
                            add(ContainerRegistryUiController::getAll)
                            add("{}", ContainerRegistryUiController::getRegistry)
                        }
                    }

                    with("/users") {
                        with(beforeHandler = createRoleGuard(Role.VIEWER)) {
                            add(UsersUiController::getAll, beforeHandler = createRootRoleGuard(Role.ADMINISTRATOR))
                        }
                        add("/{}", UsersUiController::get)
                    }

                    with("/scans") {
                        add("/{}", ScansUiController::get)

                        add("/{}/known-vulnerabilities", ScansUiController::getKnownVulnerabilities)
                        add("/{}/known-vulnerabilities/badge", ScansUiController::getKnownVulnerabilitiesBadge)
                        add("/{}/known-vulnerabilities/row-details/{}", ScansUiController::getKnownVulnerabilityDetails)

                        add("/{}/oss-packages", ScansUiController::getOssPackages)
                        add("/{}/oss-packages/badge", ScansUiController::getOssPackagesBadge)
                        add("/{}/oss-packages/row-details/{}", ScansUiController::getOssPackageDetails)

                        add("/start/{}/{}", StartScanUiController::getStartScan)
                        add("/start/configure", StartScanUiController::postConfigure)
                        with("/schedules") {
                            add("/{}", ScanScheduleUiController::get)
                        }
                    }

                    with("/jobs", beforeFunction = ::requireAdministrator) {
                        add(JobsUiController::getAll)
                        add("/table/row-details/{}", JobsUiController::getRowDetails)
                        add("/{}", JobsUiController::get)
                        add("/{}/restart", JobsUiController::postRestart)
                    }
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

    fun createRootRoleGuard(vararg role: Role): (RequestContext) -> Unit {
        return { context ->
            if (!context.request.userSession.hasRootRole(*role)) {
                throw ReturnStatus.Unauthorized401
            }
        }
    }


    fun createRoleGuard(vararg role: Role): (RequestContext) -> Unit {
        return { context ->
            if (!context.request.userSession.hasRole(*role)) {
                throw ReturnStatus.Unauthorized401
            }
        }
    }
}