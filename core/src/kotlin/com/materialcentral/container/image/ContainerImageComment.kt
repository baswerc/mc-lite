package com.materialcentral.container.image

import org.geezer.comment.Comment

class ContainerImageComment(
    val containerImageId: Long,
    message: String,
    userId: Long?,
    createdAt: Long,
    updatedAt: Long
) : Comment(message, userId, createdAt, updatedAt) {

}