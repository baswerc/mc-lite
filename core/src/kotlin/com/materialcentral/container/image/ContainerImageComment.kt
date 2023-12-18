package com.materialcentral.container.image

import com.materialcentral.user.User
import com.materialcentral.user.UsersTable
import org.geezer.comment.Comment
import org.geezer.user.GeezerUser

class ContainerImageComment(
    val containerImageId: Long,
    message: String,
    userId: Long?,
    createdAt: Long,
    updatedAt: Long
) : Comment(message, userId, createdAt, updatedAt) {

    override val user: User?
        get() = UsersTable.findById(userId)
}