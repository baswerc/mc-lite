package com.materialcentral.scan.filter

import com.materialcentral.CandidateMatchType
import org.geezer.db.FilteredUpdateStatement
import com.materialcentral.scan.FindingType
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.enum
import org.geezer.db.schema.stringList
import org.jetbrains.exposed.sql.ResultRow

object ScanFindingFiltersTable : DataTable<ScanFindingFilter>("scan_finding_filters") {

    val name = name()

    val description = description()

    val active = active()

    val candidateMatchType = enum("candidate_match_type_id", CandidateMatchType)

    val findingType = enum("finding_type_id", FindingType)

    val reason = enum("reason_id", ScanFindingFilterReason)

    var findingPrimaryIdentifierPatterns = stringList("finding_primary_identifier_patterns")

    var findingSecondaryIdentifierPatterns = stringList("finding_secondary_identifier_patterns")

    var locationPatterns = stringList("location_patterns")

    val createdAt = long("created_at")

    override fun mapDataToStatement(filter: ScanFindingFilter, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[candidateMatchType] = filter.candidateMatchType
        statement[findingType] = filter.findingType
        statement[active] = filter.active
        statement[reason] = filter.reason
        statement[name] = filter.name
        statement[description] = filter.description
        statement[findingPrimaryIdentifierPatterns] = filter.findingPrimaryIdentifierPatterns
        statement[findingSecondaryIdentifierPatterns] = filter.findingSecondaryIdentifierPatterns
        statement[locationPatterns] = filter.locationPatterns
        statement[createdAt] = filter.createdAt
    }

    override fun constructData(row: ResultRow): ScanFindingFilter {
        return ScanFindingFilter(row[name], row[description], row[active], row[candidateMatchType], row[findingType], row[reason], row[findingPrimaryIdentifierPatterns], row[findingSecondaryIdentifierPatterns],
            row[locationPatterns], row[createdAt])
    }
}