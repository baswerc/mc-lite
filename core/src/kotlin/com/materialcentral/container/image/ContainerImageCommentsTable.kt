package com.materialcentral.container.image

import com.materialcentral.user.UsersTable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.geezer.comment.CommentsTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object ContainerImageCommentsTable : CommentsTable<ContainerImageComment>("container_image_comments") {
    override val userId: Column<Long?> = long("user_id").referencesWithStandardNameAndIndex(UsersTable.id, ReferenceOption.SET_NULL).nullable()

    val containerImageId = long("container_image_id").referencesWithStandardNameAndIndex(ContainerImagesTable.id, ReferenceOption.CASCADE)

    override fun mapComment(comment: ContainerImageComment, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[containerImageId] = comment.containerImageId
    }

    override fun constructData(row: ResultRow): ContainerImageComment {
        return ContainerImageComment(row[containerImageId], row[message], row[userId], row[createdAt], row[updatedAt])
    }
}