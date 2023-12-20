package com.materialcentral.scan.schedule

import com.materialcentral.scan.ScanTargetSourceType
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.enum
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object ScanScheduleOwnersTable : DataTable<ScanScheduleOwner>("scan_finding_filter_owners") {
    val scanScheduleId = long("scan_schedule_id").referencesWithStandardNameAndIndex(ScanSchedulesTable.id, ReferenceOption.CASCADE)

    val scanTargetSourceType = enum("scan_target_source_id", ScanTargetSourceType)

    val scanTargetSourceId = long("scan_target_source_id")

    override fun mapDataToStatement(owner: ScanScheduleOwner, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[scanScheduleId] = owner.scanScheduleId
            statement[scanTargetSourceType] = owner.scanTargetSourceType
            statement[scanTargetSourceId] = owner.scanTargetSourceId
        }
    }

    override fun constructData(row: ResultRow): ScanScheduleOwner {
        return ScanScheduleOwner(row[scanScheduleId], row[scanTargetSourceType], row[scanTargetSourceId])
    }
}