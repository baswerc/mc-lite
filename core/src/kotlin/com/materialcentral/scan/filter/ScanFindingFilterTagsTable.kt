package com.materialcentral.scan.filter

import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.tag.TagsJoinTable
import org.jetbrains.exposed.sql.ReferenceOption

object ScanFindingFilterTagsTable : TagsJoinTable("scan_finding_filter_tags") {
    val scanFindingFilterId = long("scan_finding_filter_id").referencesWithStandardNameAndIndex(ScanFindingFiltersTable.id, ReferenceOption.CASCADE)

    init {
        joinTableUniqueConstraint()
    }
}