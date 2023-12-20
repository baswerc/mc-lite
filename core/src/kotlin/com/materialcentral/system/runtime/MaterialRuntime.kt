package com.materialcentral.system.runtime

import arrow.core.nonEmptyListOf
import com.materialcentral.db.Schema
import com.materialcentral.io.Routes
import org.geezer.db.connection.DatabaseInitializer
import org.geezer.system.runtime.AppExtension
import org.geezer.system.runtime.AppRuntime
import org.geezer.system.runtime.env.EnvironmentVariables
import org.geezer.causeMessage
import org.geezer.task.TaskDatabaseQueue
import org.geezer.task.TaskDispatcher
import org.geezer.task.TaskTypeQueuePrimer
import org.geezer.task.TaskTypeRegistry
import org.reflections.Reflections

object MaterialRuntime : AppRuntime() {

    override val tasksInUse: Boolean = true

    private var jobDispatcherTransient: Boolean = false

    private var taskDispatcher: TaskDispatcher? = null

    fun start() {
        try {
            if (_active) {
                return
            }

            log.info("MaterialCentral starting.")

            DatabaseInitializer.initialize()
            Schema.initialize()
            TaskTypeRegistry.loadTaskTypesFromSchema()

            val uiActive = uiActive()
            val apiActive = apiActive()
            if (uiActive || apiActive) {
                Routes.initialize(uiActive, apiActive)
            }

            jobDispatcherTransient = "true".equals(EnvironmentVariables["JobDispatcherTransient"], true)
            if (jobDispatcherActive()) {
                log.info("Starting task executor.")
                taskDispatcher = TaskDispatcher(nonEmptyListOf(TaskDatabaseQueue), listOf(TaskTypeQueuePrimer), jobDispatcherTransient).apply { start() }
            }

            log.info("MaterialCentral running.")
            _active = true
        } catch (e: Exception) {
            log.error("MaterialCentral startup failed due to ${e.causeMessage}", e)
            throw e
        }
    }

    fun stop() {
        if (!_active) {
            return
        }

        log.info("MaterialCentral stopping.")
        _active = false

        try {
            taskDispatcher?.stop()
        } catch (e: Exception) {
            log.warn("Task dispatcher failed to stop gracefully: ${e.causeMessage}", e)
        }

        DatabaseInitializer.close()

        log.info("MaterialCentral shutdown.")
    }
}