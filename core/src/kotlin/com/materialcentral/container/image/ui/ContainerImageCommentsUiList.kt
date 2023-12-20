package com.materialcentral.container.image.ui

import org.geezer.comment.CommentUiList
import com.materialcentral.container.image.ContainerImageCommentsTable
import jakarta.servlet.http.HttpServletRequest
import org.geezer.routes.urls.UrlGen
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.select

class ContainerImageCommentsUiList(val containerImageId: Long) : CommentUiList<ContainerImageCommentsTable>(ContainerImageCommentsTable) {
    override val htmlId: String = "imageComments-$containerImageId"

    override fun createQuery(): Query {
        return ContainerImageCommentsTable.select { ContainerImageCommentsTable.containerImageId eq containerImageId }
    }

    override fun createGetCommentsUrl(request: HttpServletRequest): String {
        return UrlGen.url(ContainerImagesUiController::getCommentsList, containerImageId, request)
    }

    override fun createAddCommentUrl(request: HttpServletRequest): String {
        return UrlGen.url(ContainerImagesUiController::postCreateComment, containerImageId, request)
    }

    override fun createUpdateCommentUrl(request: HttpServletRequest): String {
        return UrlGen.url(ContainerImagesUiController::postUpdateComment, containerImageId, request)
    }

    override fun createDeleteCommentUrl(request: HttpServletRequest): String {
        return UrlGen.url(ContainerImagesUiController::postDeleteComment, containerImageId, request)
    }
}