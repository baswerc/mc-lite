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
        return UrlGen.url(ContainerImageUiController::getCommentsList, containerImageId, request)
    }

    override fun createAddCommentUrl(request: HttpServletRequest): String {
        return UrlGen.url(ContainerImageUiController::postCreateComment, containerImageId, request)
    }

    override fun createUpdateCommentUrl(request: HttpServletRequest): String {
        return UrlGen.url(ContainerImageUiController::postUpdateComment, containerImageId, request)
    }

    override fun createDeleteCommentUrl(request: HttpServletRequest): String {
        return UrlGen.url(ContainerImageUiController::postDeleteComment, containerImageId, request)
    }
}