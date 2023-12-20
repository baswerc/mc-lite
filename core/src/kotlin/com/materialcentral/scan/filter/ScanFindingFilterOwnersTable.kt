package com.materialcentral.scan.filter

import com.materialcentral.scan.ScanTargetSourceType
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.enum
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object ScanFindingFilterOwnersTable : DataTable<ScanFindingFilterOwner>("scan_finding_filter_owners") {
    val scanFindingFilterId = long("scan_finding_filter_id").referencesWithStandardNameAndIndex(ScanFindingFiltersTable.id, ReferenceOption.CASCADE)

    val scanTargetSourceType = enum("scan_target_source_id", ScanTargetSourceType)

    val scanTargetSourceId = long("scan_target_source_id")

    override fun mapDataToStatement(owner: ScanFindingFilterOwner, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[scanFindingFilterId] = owner.scanFindingFilterId
            statement[scanTargetSourceType] = owner.scanTargetSourceType
            statement[scanTargetSourceId] = owner.scanTargetSourceId
        }
    }

    override fun constructData(row: ResultRow): ScanFindingFilterOwner {
        return ScanFindingFilterOwner(row[scanFindingFilterId], row[scanTargetSourceType], row[scanTargetSourceId])
    }
}