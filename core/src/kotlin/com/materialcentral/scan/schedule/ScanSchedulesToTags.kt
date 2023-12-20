package com.materialcentral.scan.schedule

import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.tag.TagsJoinTable
import org.jetbrains.exposed.sql.ReferenceOption

object ScanSchedulesToTags : TagsJoinTable("scan_schedules_to_tags") {
    val scanScheduleId = long("scan_schedule_id").referencesWithStandardNameAndIndex(ScanSchedulesTable.id, ReferenceOption.CASCADE)

    init {
        joinTableUniqueConstraint()
    }
}