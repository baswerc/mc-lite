package com.materialcentral.container.image.ui

import com.materialcentral.container.ContainerUiController
import org.geezer.io.set
import org.geezer.io.ui.table.isUiTableRequest
import com.materialcentral.container.image.*
import org.geezer.system.runtime.RuntimeClock
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.html.span
import kotlinx.html.stream.appendHTML
import org.geezer.io.ui.*
import org.geezer.routes.RequestParameters
import org.geezer.routes.ReturnStatus
import org.geezer.routes.TerminateRouteException
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

object ContainerImagesUiController : UiController() {
    override val jspPath: String by lazy { "${ContainerUiController.jspPath}/images" }

    fun getImageRowDetails(id: Long, request: HttpServletRequest): String {
        val (_, _, image) = findImage(id)

        request.pageObject = image
        request["tags"] = ContainerImageTagsTable.getActiveTagsForImage(id).map { it.value }
        request["deployments"] = ContainerImageDeploymentsTable.findActiveDeploymentsFor(id).mapNotNull { EnvironmentCache[it.environmentId]?.name }
        return rowDetailsJsp
    }

    @JvmField
    val ImageRoute = ::getImage
    fun getImage(id: Long, request: HttpServletRequest): String {
        val coordinates = findImageIfAuthorized(id, request, Role.VIEWER)
        request["baseImage"] = coordinates.image.baseContainerImageId?.let {ContainerImageCoordinates.findById(it) }
        return setupViewRequest(jspp("attributes.jsp"), coordinates, request)
    }

    @JvmField
    val TagsRoute = ::getTags
    fun getTags(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val coordinates = findImageIfAuthorized(id, request, Role.VIEWER)
        val table = ImageTagsUiTable(id)
        if (request.isUiTableRequest) {
            table.toHTML(request, parameters, response)
            throw TerminateRouteException()
        }

        request["table"] = table
        return setupViewRequest(component("tablePanel.jsp"), coordinates, request)
    }

    @JvmField
    val TagsBadgeRoute = ::getTagsBadge
    fun getTagsBadge(id: Long, request: HttpServletRequest, response: HttpServletResponse) {
        findImageIfAuthorized(id, request, Role.VIEWER)
        val count = ContainerImageTagsTable.select { ContainerImageTagsTable.containerImageId eq id }.count()
        response.writer.appendHTML().badge(UI.formatNumber(count))
    }

    @JvmField
    val LayersRoute = ::getLayers
    fun getLayers(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val coordinates = findImageIfAuthorized(id, request, Role.VIEWER)
        val table = ImageLayersUiTable(id)
        if (request.isUiTableRequest) {
            table.toHTML(request, parameters, response)
            throw TerminateRouteException()
        }

        request["table"] = table
        return setupViewRequest(component("tablePanel.jsp"), coordinates, request)
    }

    @JvmField
    val LayersBadgeRoute = ::getLayersBadge
    fun getLayersBadge(id: Long, request: HttpServletRequest, response: HttpServletResponse) {
        findImageIfAuthorized(id, request, Role.VIEWER)
        val count = ContainerImageLayersTable.select { ContainerImageLayersTable.containerImageId eq id }.count()
        response.writer.appendHTML().badge(UI.formatNumber(count))
    }

    @JvmField
    val ScanSchedulesRoute = ::getScanSchedules
    fun getScanSchedules(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val coordinates = findImageIfAuthorized(id, request, Role.VIEWER)
        val table = ScanSchedulesUiTable(coordinates.image, false, false)
        if (request.isUiTableRequest) {
            table.toHTML(request, parameters, response)
            throw TerminateRouteException()
        }

        request["table"] = table
        return setupViewRequest(component("tablePanel.jsp"), coordinates, request)
    }

    fun getScanScheduleDetails(imageId: Long, scheduleId: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse): String {
        val coordinates = findImageIfAuthorized(imageId, request, Role.VIEWER)
        val schedule = findScanScheduleForImage(coordinates.image, scheduleId)

        request.pageObject = coordinates
        request["schedule"] = schedule
        request["canScan"] = request.userSession.hasRoleFor(coordinates.image, *Scan.ScanRoles)
        return jspp("scheduleDetails.jsp")
    }

    @JvmField
    val StartScanScheduleRoute = ::postStartScheduleScan
    fun postStartScheduleScan(imageId: Long, scheduleId: Long, request: HttpServletRequest): String {
        val coordinates = findImageIfAuthorized(imageId, request, *Scan.ScanRoles)
        val schedule = findScanScheduleForImage(coordinates.image, scheduleId)

        val scan = ScansTable.create(Scan(coordinates.image, schedule))
        return scan.redirectUrl(request)
    }

    @JvmField
    val CommentsRoute = ::getComments
    fun getComments(id: Long, request: HttpServletRequest): String {
        val coordinates = findImageIfAuthorized(id, request, Role.VIEWER)
        request["list"] = ContainerImageCommentsUiList(id)
        return setupViewRequest("comments.jsp", coordinates, request)
    }

    @JvmField
    val CommentsBadgeRoute = ::getCommentsBadge
    fun getCommentsBadge(id: Long, request: HttpServletRequest, response: HttpServletResponse) {
        findImageIfAuthorized(id, request, Role.VIEWER)
        val count = ContainerImageCommentsTable.select { ContainerImageCommentsTable.containerImageId eq id }.count()
        response.writer.appendHTML().span("badge text-bg-secondary float-end") {
            text(UI.formatNumber(count))
        }
    }


    fun getCommentsList(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse) {
        findImageIfAuthorized(id, request, Role.VIEWER)
        ContainerImageCommentsUiList(id).
        toHtml(request, parameters, response, request.user, request.user.superAdministrator)
    }


    fun postUpdateComment(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse) {
        findImageIfAuthorized(id, request, Role.VIEWER)

        val commentId = parameters.getLong(CommentUiList.IdParameter)
        if (commentId == null) {
            throw ReturnStatus.BadRequest400
        }

        val message = parameters[CommentUiList.MessageParameter]
        if (message.isNullOrBlank()) {
            throw ReturnStatus.BadRequest400
        }

        val comment = ContainerImageCommentsTable.findUniqueWhere { (ContainerImageCommentsTable.containerImageId eq id) and (ContainerImageCommentsTable.id eq commentId) }
        if (comment == null) {
            throw ReturnStatus.NotFound404
        }

        comment.message = message
        comment.updatedAt = RuntimeClock.now

        ContainerImageCommentsTable.update(comment, ContainerImageCommentsTable.message, ContainerImageCommentsTable.updatedAt)
        ContainerImageCommentsUiList(id).onCommentAdded(comment, request, parameters, response, request.user, request.user.superAdministrator)
    }

    fun postCreateComment(id: Long, request: HttpServletRequest, parameters: RequestParameters, response: HttpServletResponse) {
        findImageIfAuthorized(id, request, Role.VIEWER)

        val message = parameters[CommentUiList.MessageParameter]
        if (message.isNullOrBlank()) {
            throw ReturnStatus.BadRequest400
        }

        val now = RuntimeClock.now
        val comment = ContainerImageCommentsTable.create(ContainerImageComment(id, message, request.user.id, now, now))
        ContainerImageCommentsUiList(id).onCommentAdded(comment, request, parameters, response, request.user, request.user.superAdministrator)
    }

    fun postDeleteComment(id: Long, request: HttpServletRequest, parameters: RequestParameters) {
        findImageIfAuthorized(id, request, Role.VIEWER)

        val commentId = parameters.getLong(CommentUiList.IdParameter)
        if (commentId == null) {
            throw ReturnStatus.BadRequest400
        }

        val comment = ContainerImageCommentsTable.findUniqueWhere { (ContainerImageCommentsTable.containerImageId eq id) and (ContainerImageCommentsTable.id eq commentId) }
        if (comment == null) {
            throw ReturnStatus.NotFound404
        }

        ContainerImageCommentsTable.delete(commentId)
    }

    @JvmField
    val ImageSynchronizeRoute = ::postSynchronize
    fun postSynchronize(id: Long, request: HttpServletRequest): String {
        val (_, _, image) = findImageIfAuthorized(id, request, Role.OWNER, Role.SECURITY_OFFICER)

        return ContainerImageSynchronizationsTable.create(ContainerImageSynchronization(image)).redirectUrl(request)
    }
    @JvmField
    val StartScanRoute = ::getStartScan
    fun getStartScan(id: Long, request: HttpServletRequest, response: HttpServletResponse): String {
        val coordinates = findImageIfAuthorized(id, request, Role.OWNER, Role.SECURITY_OFFICER)
        return setupViewRequest("startScan.jsp", coordinates, request)
    }

    private fun findImage(id: Long): ContainerImageCoordinates {
        val coordinates = ContainerImageCoordinates.findById(id)
        if (coordinates == null) {
            throw ReturnStatus.NotFound404
        }
        return coordinates
    }

    private fun findScanScheduleForImage(image: ContainerImage, scheduleId: Long): ScanSchedule {
        val schedule = scheduleId.findOr404(ScanSchedulesTable)

        val rootPathIds = RootPathsTable.findPathToRoot(image).map { it.first }
        if (!rootPathIds.contains(schedule.parentId)) {
            throw ReturnStatus.BadRequest400
        }

        return schedule
    }

    private fun setupViewRequest(panel: String, coordinates: ContainerImageCoordinates, request: HttpServletRequest): String {
        request.pageObject = coordinates

        return if (request.hxTab) {
            request.noLayout()
            panel
        } else {
            request["canScan"] = request.userSession.hasRoleFor(coordinates.image, *Scan.ScanRoles)
            request["canSynchronize"] = request.userSession.hasRoleFor(coordinates.image, Role.EDITOR)
            request.tags = ContainerRepositoryTagsTable.findTagsFor(coordinates.repository.id)
            request["panel"] = panel
            viewJsp
        }
    }
}