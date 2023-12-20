package com.materialcentral.scan

import arrow.core.nonEmptyListOf
import com.materialcentral.DataStringsTable
import com.materialcentral.scan.schedule.ScanSchedule
import com.materialcentral.scan.schedule.ScanSchedulesTable
import com.materialcentral.user.UserRole
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.attribute
import jakarta.servlet.http.HttpServletRequest
import kotlinx.html.TagConsumer
import kotlinx.html.div
import kotlinx.html.unsafe
import org.geezer.task.Task
import org.geezer.task.TaskInitializationParameters
import org.geezer.task.TaskType

class Scan(
    val scanTargetType: ScanTargetType,
    val scanTargetId: Long,
    var scanTargetNameId: Long,
    val medium: ScanMedium,
    val configuration: ScanConfiguration,
    val scheduleId: Long?,
    var criticalFindings: Int?,
    var highFindings: Int?,
    var mediumFindings: Int?,
    var lowFindings: Int?,
    var lastSynchronizedAt: Long?,
    parameters: TaskInitializationParameters
) : Task(parameters) {

    val scannedFindingTypes: List<FindingType>
        get() = configuration.analysisConfigurations.flatMap { it.findingTypes }.distinct().sortedBy { it.label}

    override val type: TaskType = ScanTaskType

    constructor(target: ScanTarget, schedule: ScanSchedule) : this(target.scanTargetType, target.id, DataStringsTable.getOrCreate(target.getNameForScan()), schedule.medium, schedule.scanConfiguration, schedule.id)

    constructor(scanTargetType: ScanTargetType, scanTargetId: Long, scanTargetNameId: Long, scanMedium: ScanMedium, configuration: ScanConfiguration, scheduleId: Long?) :
            this(scanTargetType, scanTargetId, scanTargetNameId, scanMedium, configuration, scheduleId, null, null, null, null, null, TaskInitializationParameters())

    fun lookupScanTarget(): ScanTarget? {
        return scanTargetType.findTarget(scanTargetId)
    }

    override fun doTask() {
        TODO("Not yet implemented")
    }

    companion object {
        @JvmField
        val Icon = FontIcon("fa-radar", "e024")

        val ScanRoles = nonEmptyListOf(UserRole.OWNER, UserRole.SECURITY_OFFICER)
    }
}