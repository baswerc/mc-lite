package com.materialcentral.container.image.task

import com.materialcentral.container.image.ContainerImagesTable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.geezer.task.TaskInitializationParameters
import org.geezer.task.TaskType
import org.geezer.task.TasksTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object ContainerImageSynchronizationTasksTable : TasksTable<ContainerImageSynchronizationTask>("container_image_synchronization_tasks") {

    val containerImageId = long("container_image_id").referencesWithStandardNameAndIndex(ContainerImagesTable.id, ReferenceOption.CASCADE)

    override val type: TaskType
        get() = ContainerImageSynchronizationTaskType

    override fun constructTask(row: ResultRow, parameters: TaskInitializationParameters): ContainerImageSynchronizationTask {
        return ContainerImageSynchronizationTask(row[containerImageId], parameters)
    }

    override fun mapTask(task: ContainerImageSynchronizationTask, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[containerImageId] = task.containerImageId
        }
    }

}