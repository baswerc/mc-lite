package com.materialcentral.scan

import org.geezer.io.ui.FontIcon
import org.geezer.task.TaskType
import org.geezer.task.TasksTable

object ScanTaskType : TaskType() {
    override val table: TasksTable<*>
        get() = ScansTable

    override val id: String = "scan"

    override val description: String = ""

    override val icon: FontIcon = Scan.Icon
}