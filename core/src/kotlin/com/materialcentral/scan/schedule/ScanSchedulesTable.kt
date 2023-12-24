package com.materialcentral.scan.schedule

import com.materialcentral.CandidateMatchType
import com.materialcentral.scan.ScanConfiguration
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.ScanTargetSourceType
import com.materialcentral.schedule.DaysOfWeek
import com.materialcentral.schedule.TimeRange
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.*
import org.jetbrains.exposed.sql.ResultRow

object ScanSchedulesTable : DataTable<ScanSchedule>("repository_scan_schedules") {

    val name = name()

    val description = description()

    val active = active()

    val candidateMatchType = enum("candidate_match_type_id", CandidateMatchType)

    val scanTargetSourceTypes = list("scan_source_type_id", ScanTargetSourceType)

    val medium = enum("medium_id", ScanMedium)

    val scanConfiguration = encodedJson("scan_configuration", ScanConfiguration)

    val contributeToTargetMetadata = bool("contribute_to_target_metadata")

    val minimumHoursBetweenScans = integer("minimum_hours_between_scans").nullable()

    val timeRange = encodedJson("scan_time", TimeRange).nullable()

    val scanDays = encodedJson("scan_days", DaysOfWeek).nullable()

    val scanAllTargets = bool("scan_all_target")

    val scanDefaultTarget = bool("scan_default_target")

    val scanTargetNamePatterns = stringList("scan_target_name_patterns")

    val scanTargetEnvironmentIds = longList("scan_assets_environment_ids")

    val scanNewTargetsImmediately = bool("scan_new_targets_immediately")

    override fun mapDataToStatement(schedule: ScanSchedule, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[name] = schedule.name
        statement[description] = schedule.description
        statement[active] = schedule.active
        statement[candidateMatchType] = schedule.candidateMatchType
        statement[scanTargetSourceTypes] = schedule.scanTargetSourceTypes
        statement[medium] = schedule.medium
        statement[scanConfiguration] = schedule.scanConfiguration
        statement[contributeToTargetMetadata] = schedule.contributeToTargetMetadata
        statement[minimumHoursBetweenScans] = schedule.minimumHoursBetweenScans
        statement[timeRange] = schedule.timeRange
        statement[scanDays] = schedule.scanDays
        statement[scanAllTargets] = schedule.scanAllTargets
        statement[scanDefaultTarget] = schedule.scanDefaultTarget
        statement[scanTargetNamePatterns] = schedule.scanTargetNamePatterns
        statement[scanTargetEnvironmentIds] = schedule.scanTargetInEnvironmentIds
        statement[scanNewTargetsImmediately] = schedule.scanNewTargetsImmediately
    }

    override fun constructData(row: ResultRow): ScanSchedule {
        return ScanSchedule(row[name], row[description], row[active], row[candidateMatchType], row[scanTargetSourceTypes], row[medium], row[scanConfiguration], row[contributeToTargetMetadata], row[minimumHoursBetweenScans],
            row[timeRange], row[scanDays], row[scanAllTargets], row[scanDefaultTarget], row[scanTargetNamePatterns], row[scanTargetEnvironmentIds], row[scanNewTargetsImmediately])
    }
}