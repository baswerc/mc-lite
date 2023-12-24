package com.materialcentral.container.image.task

import org.geezer.io.ui.FontIcon
import org.geezer.task.TaskType
import org.geezer.task.TasksTable

object ContainerImageSynchronizationTaskType : TaskType() {

    override val id: String = "container-image-synchronization"

    override val table: TasksTable<*>
        get() = ContainerImageSynchronizationTasksTable

    override val description: String = ""

    override val icon: FontIcon = FontIcon.Synchronize
}