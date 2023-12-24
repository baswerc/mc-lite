package com.materialcentral.container.repository.ui

import com.materialcentral.container.ContainerUiController
import com.materialcentral.container.image.ContainerImagesTable
import com.materialcentral.container.registry.ContainerRegistriesTable
import com.materialcentral.container.registry.ContainerRegistry
import com.materialcentral.container.repository.ContainerRepositoriesTable
import com.materialcentral.container.repository.ContainerRepository
import com.materialcentral.container.repository.ContainerRepositoryCoordinates
import com.materialcentral.container.repository.ContainerRepositoryTagsTable
import com.materialcentral.container.repository.task.ContainerRepositorySynchronizationTask
import com.materialcentral.container.repository.task.ContainerRepositorySynchronizationTasksTable
import com.materialcentral.io.ui.tags
import org.geezer.io.ui.table.isUiTableRequest
import org.geezer.system.runtime.RuntimeClock
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.html.span
import kotlinx.html.stream.appendHTML
import org.geezer.io.error
import org.geezer.io.hasWarningsOrErrors
import org.geezer.io.set
import org.geezer.io.ui.*
import org.geezer.routes.RequestParameters
import org.geezer.routes.ReturnStatus
import org.geezer.routes.TerminateRouteException
import org.geezer.routes.route
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select

object ContainerRepositoryUiController : UiController() {
    override val jspPath: String by lazy { "${ContainerUiController.jspPath}/repositories" }

    val panelsJspPath: String by lazy { "$jspPath/panels" }

    @JvmField
    val GetAllRoute = ::getAll
    fun getAll(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        if (request.isUiTableRequest) {
            ContainerRepositoryUiTable.toHTML(request, parameters, response)
            throw TerminateRouteException(true)
        }

        return indexJsp
    }

    fun getAllTableDetail(id: Long, request: HttpServletRequest): String {
        val repository = id.findOr404(ContainerRepositoriesTable)
        request.pageObject = repository
        return rowDetailsJsp
    }

    @JvmField
    val GetRepositoryRoute = ::getRepository
    fun getRepository(id: Long, request: HttpServletRequest): String {
        return setupViewRequest("attributes.jsp", findContainerRepository(id, request), request)
    }

    @JvmField
    val GetImagesRoute = ::getImages
    fun getImages(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val coordinates = findContainerRepository(id, request)
        val table = ContainerRepositoryImagesUiTable(coordinates.repository)

        if (request.isUiTableRequest) {
            table.toHTML(request, parameters, response)
            throw TerminateRouteException()
        }

        request["table"] = table
        return setupViewRequest("images.jsp", coordinates, request)
    }

    @JvmField
    val GetImagesBadgeRoute = ::getImagesBadge
    fun getImagesBadge(id: Long, request: HttpServletRequest, response: HttpServletResponse) {
        val count = ContainerImagesTable.select { ContainerImagesTable.containerRepositoryId eq id }.count()
        response.writer.appendHTML().badge(UI.formatNumber(count))
    }

    @JvmField
    val PoliciesRoute = ::getPolicies
    fun getPolicies(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val coordinates = findContainerRepository(id, request)
        return setupViewRequest("policies.jsp", coordinates, request)
    }

    @JvmField
    val PoliciesBadgeRoute = ::getPoliciesBadge
    fun getPoliciesBadge(id: Long, request: HttpServletRequest, response: HttpServletResponse) {
        val (_, repository) = findContainerRepository(id, request)
        response.writer.appendHTML().span("badge text-bg-secondary float-end") {
            text(UI.formatNumber(24))
        }
    }

    @JvmField
    val ViolationsRoute = ::getViolations
    fun getViolations(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val coordinates = findContainerRepository(id, request)
        return setupViewRequest("violations.jsp", coordinates, request)
    }

    @JvmField
    val ViolationsBadgeRoute = ::getViolationsBadge
    fun getViolationsBadge(id: Long, request: HttpServletRequest, response: HttpServletResponse) {
        val (_, repository) = findContainerRepository(id, request)
        response.writer.appendHTML().span("float-end") {
            span("ms-1 badge bg-danger") { text("5 C")}
            span("ms-1 badge bg-warning") { text("12 H")}
        }
    }

    @JvmField
    val PostSynchronizeNewImagesRoute = ::postSynchronizeNewImages
    fun postSynchronizeNewImages(id: Long, request: HttpServletRequest): String {
        val (_, repository) = findContainerRepository(id, request)
        return ContainerRepositorySynchronizationTasksTable.create(ContainerRepositorySynchronizationTask(repository, false)).redirectUrl(request)
    }

    @JvmField
    val PostFullSynchronizationRoute = ::postFullSynchronization
    fun postFullSynchronization(id: Long, request: HttpServletRequest): String {
        val (_, repository) = findContainerRepository(id, request)
        return ContainerRepositorySynchronizationTasksTable.create(ContainerRepositorySynchronizationTask(repository, false)).redirectUrl(request)
    }

    @JvmField
    val EvaluatePoliciesRoute = ::postEvaluatePolicies
    fun postEvaluatePolicies(id: Long, request: HttpServletRequest): String {
        val (_, repository) = findContainerRepository(id, request, Role.OWNER)
        return PoliciesEvaluationsTable.create(PoliciesEvaluation(repository)).redirectUrl(request)
    }

    fun getEdit(id: Long, request: HttpServletRequest): String {
        val (registry, repository) = findContainerRepository(id, request)
        setupSaveRequest(registry, repository, request)
        return editJsp
    }

    fun postUpdate(id: Long, request: HttpServletRequest, parameters: RequestParameters): String {
        val (registry, repository) = findContainerRepository(id, request)

        update(repository, request, parameters)

        return if (request.hasWarningsOrErrors()) {
            setupSaveRequest(registry, repository, request)
            editJsp
        } else {
            ContainerRepositoriesTable.update(repository)
            repository.redirectUrl(request)
        }
    }

    fun getNew(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val repository = initializeRepository(request, parameters)
        setupSaveRequest(null, repository, request)
        return jspp("new.jsp")
    }

    fun postCreate(request: HttpServletRequest, parameters: RequestParameters): String {
        val repository = initializeRepository(request, parameters)

        update(repository, request, parameters)

        return if (request.hasWarningsOrErrors()) {
            setupSaveRequest(null, repository, request)
            newJsp
        } else {
            ContainerRepositoriesTable.update(repository)
            repository.redirectUrl(request)
        }
    }

    
    private fun setupViewRequest(panel: String, coordinates: ContainerRepositoryCoordinates, request: HttpServletRequest): String {
        request.pageObject = coordinates.repository
        request["registry"] = coordinates.registry

        return if (request.hxTab) {
            request.noLayout()
            "$panelsJspPath/$panel"
        } else {
            request.tags = ContainerRepositoryTagsTable.findCachedTagsFor(coordinates.repository.id)
            request["panel"] = panel
            viewJsp
        }
    }

    private fun setupSaveRequest(registry: ContainerRegistry?, repository: ContainerRepository, request: HttpServletRequest, registries: List<ContainerRegistry> = ContainerRegistriesTable.findAllActive(ContainerRegistriesTable.hostname to SortOrder.ASC)) {
        request.pageObject = repository
        request["registry"] = registry
        request["registries"] = registries
    }

    private fun update(repository: ContainerRepository, request: HttpServletRequest, parameters: RequestParameters) {
        val registry = ContainerRegistriesTable.findById(parameters.getLong("registry"))
        if (registry == null || !registry.active) {
            request.error("A container registry is required.")
        } else {
            repository.containerRegistryId = registry.id
        }

        val name = parameters["name"]?.removePrefix("/")
        if (name.isNullOrBlank()) {
            request.error("Name is required.")
        } else if (registry != null && !name.equals(repository.name, true) && ContainerRepositoriesTable.nameExistsInRegistry(registry.id, name)) {
            request.error("A repository with the provided name already exists.")
        } else {
            repository.name = name
        }

        repository.description = parameters["description"]
        repository.active = parameters.getBoolean("active", repository.active)
    }

    private fun findContainerRepository(id: Long, request: HttpServletRequest): ContainerRepositoryCoordinates {
        val coordinates = ContainerRepositoryCoordinates.findById(id) ?: throw ReturnStatus.NotFound404
        return coordinates
    }

    private fun initializeRepository(request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): ContainerRepository  {

        val registries = ContainerRegistriesTable.findAll().sortedBy { it.name }

        if (registries.isEmpty()) {
            throw request.redirectToReferrer(response)
        }

        val parentId = parameters.getLong("parentId") ?: run {
            log.warn("Received new container repository request: ${request.route} without parentId parameter from user: ${request.optionalUser}")
            throw ReturnStatus.BadRequest400
        }

        val parentType = MaterialGroupType.mapIfValid(parameters.getInt("parentType")) ?: run {
            log.warn("Received new container repository request: ${request.route} without parentType parameter from user: ${request.optionalUser}")
            throw ReturnStatus.BadRequest400
        }

        if (!ContainerRepository.ValidParentTypes.contains(parentType)) {
            log.warn("Received new container repository request: ${request.route} with invalid parent type: $parentType from user: ${request.optionalUser}")
            throw ReturnStatus.BadRequest400
        }

        val parent = MaterialGroupsTable.findParent(parentId, parentType) ?: run {
            log.warn("Received new container repository request: ${request.route} with parent type: $parentType and id: $parentId that could not be found from user: ${request.optionalUser}")
            throw ReturnStatus.BadRequest400
        }

        return ContainerRepository(0, null, false, null, null, null, "", null, parentId, parentType, true, RuntimeClock.now, null)
    }
}