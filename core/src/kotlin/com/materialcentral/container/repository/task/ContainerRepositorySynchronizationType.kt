package com.materialcentral.container.repository.task

import com.materialcentral.container.repository.ContainerRepositoriesTable
import com.materialcentral.container.repository.ContainerRepositorySettings
import org.geezer.io.ui.FontIcon
import org.geezer.system.runtime.DataEnumProperty
import org.geezer.system.runtime.IntProperty
import org.geezer.system.runtime.RuntimeClock
import org.geezer.task.*
import org.jetbrains.exposed.sql.*

object ContainerRepositorySynchronizationType : FixedInternalTaskType() {
    override val id: String = "container-repository-synchronization"

    override val description: String? = ""

    override val minSecondsBetweenCreateTasks: IntProperty = IntProperty("ContainerRepositorySynchronizationMinSeconds", 60)

    override fun createTasks() {
        val settings = ContainerRepositorySettings.get()
        val maxLastNewImagesCheckAt = RuntimeClock.nowMinusMinutes(settings.minMinutesBetweenNewImagesCheck)
        val containerRepositoryValues = ContainerRepositoriesTable.slice(ContainerRepositoriesTable.id, ContainerRepositoriesTable.lastFullSynchronizationAt).select {
            (ContainerRepositoriesTable.active eq true) and
                    (ContainerRepositoriesTable.lastNewImagesCheckAt.isNull() or (ContainerRepositoriesTable.lastNewImagesCheckAt lessEq maxLastNewImagesCheckAt)) and
                    notExists(ContainerRepositorySynchronizationTasksTable.select { (ContainerRepositorySynchronizationTasksTable.containerRepositoryId eq ContainerRepositoriesTable.id) and
                            (ContainerRepositorySynchronizationTasksTable.state inList TaskState.activeStates)})
        }.map { it[ContainerRepositoriesTable.id] to it[ContainerRepositoriesTable.lastFullSynchronizationAt] }


        if (containerRepositoryValues.isEmpty()) {
            return
        }

        val maxLastFullSynchronizationAt = RuntimeClock.nowMinusMinutes(settings.minMinutesBetweenFullSynchronization)
        for ((containerRepositoryId, lastFullSynchronizationAt) in containerRepositoryValues) {
            val fullSynchronization = lastFullSynchronizationAt == null || lastFullSynchronizationAt < maxLastFullSynchronizationAt
            ContainerRepositorySynchronizationTasksTable.create(ContainerRepositorySynchronizationTask(containerRepositoryId, fullSynchronization, TaskInitializationParameters(priority = jobPriority())))
        }
    }

    override val table: TasksTable<*>
        get() = ContainerRepositorySynchronizationTasksTable

    override val icon: FontIcon = FontIcon.Synchronize

    val jobPriority = DataEnumProperty("ContainerRepositorySynchronizationPriority", TaskPriority) { TaskPriority.LOW }
}