package com.materialcentral.scan

import com.materialcentral.DataStringsTable
import org.geezer.db.FilteredUpdateStatement
import com.materialcentral.scan.schedule.ScanSchedule
import com.materialcentral.scan.schedule.ScanSchedulesTable
import org.geezer.db.schema.encodedJson
import org.geezer.db.schema.enum
import org.geezer.db.schema.indexWithStandardName
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.geezer.task.TaskInitializationParameters
import org.geezer.task.TaskType
import org.geezer.task.TasksTable
import org.jetbrains.exposed.sql.*

object ScansTable : TasksTable<Scan>("scans") {

    val scanTargetType = enum("scan_target_type_id", ScanTargetType).indexWithStandardName()

    val scanTargetId = long("scan_target_id").indexWithStandardName()

    val scanTargetNameId = long("scan_target_name_id").referencesWithStandardNameAndIndex(DataStringsTable.id, ReferenceOption.NO_ACTION)

    val medium = enum("medium_id", ScanMedium)

    val configuration = encodedJson("scan_configuration", ScanConfiguration)

    val scheduleId = long("schedule_id").referencesWithStandardNameAndIndex(ScanSchedulesTable.id, ReferenceOption.SET_NULL).nullable()

    val criticalFindings = integer("critical_findings").nullable()

    val highFindings = integer("high_findings").nullable()

    val mediumFindings = integer("medium_findings").nullable()

    val lowFindings = integer("low_findings").nullable()

    val lastSynchronizedAt = long("last_synchronized_at").nullable()

    override val type: TaskType
        get() = ScanTaskType


    fun findLatestScansFor(schedules: List<ScanSchedule>): List<Scan> {
        if (schedules.isEmpty()) {
            return listOf()
        }
        val scansSubQuery = alias("subquery")
        return ScansTable.select { (scheduleId inList schedules.map { it.id }) and
                (ScansTable.createdAt inSubQuery (scansSubQuery.slice(scansSubQuery[createdAt].max()).select { (scansSubQuery[scheduleId] eq scheduleId) } ))}.map { map(it) }
    }

    override fun constructTask(row: ResultRow, parameters: TaskInitializationParameters): Scan {
        return Scan(row[scanTargetType], row[scanTargetId], row[scanTargetNameId], row[medium], row[configuration], row[scheduleId],
            row[criticalFindings], row[highFindings], row[mediumFindings], row[lowFindings], row[lastSynchronizedAt], parameters)
    }

    override fun mapTask(scan: Scan, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[scanTargetType] = scan.scanTargetType
            statement[scanTargetId] = scan.scanTargetId
            statement[medium] = scan.medium
            statement[configuration] = scan.configuration
            statement[scheduleId] = scan.scheduleId
        }

        statement[scanTargetNameId] = scan.scanTargetNameId
        statement[criticalFindings] = scan.criticalFindings
        statement[highFindings] = scan.highFindings
        statement[mediumFindings] = scan.mediumFindings
        statement[lowFindings] = scan.lowFindings
        statement[lastSynchronizedAt] = scan.lastSynchronizedAt
    }

}