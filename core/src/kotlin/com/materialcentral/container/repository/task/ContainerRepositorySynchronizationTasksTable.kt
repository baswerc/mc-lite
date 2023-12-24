package com.materialcentral.container.repository.task

import com.materialcentral.container.repository.ContainerRepositoriesTable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.geezer.task.TaskInitializationParameters
import org.geezer.task.TaskType
import org.geezer.task.TasksTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object ContainerRepositorySynchronizationTasksTable : TasksTable<ContainerRepositorySynchronizationTask>("container_repository_synchronization_tasks") {

    val containerRepositoryId = long("container_repository_id").referencesWithStandardNameAndIndex(ContainerRepositoriesTable.id, ReferenceOption.CASCADE)

    val fullSynchronization = bool("full_synchronization")

    override val type: TaskType
        get() = ContainerRepositorySynchronizationType

    override fun constructTask(row: ResultRow, parameters: TaskInitializationParameters): ContainerRepositorySynchronizationTask {
        return ContainerRepositorySynchronizationTask(row[containerRepositoryId], row[fullSynchronization], parameters)
    }

    override fun mapTask(task: ContainerRepositorySynchronizationTask, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[containerRepositoryId] = task.containerRepositoryId
            statement[fullSynchronization] = task.fullSynchronization
        }
    }
}