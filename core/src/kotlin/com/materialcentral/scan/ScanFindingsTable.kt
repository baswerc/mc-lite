package com.materialcentral.scan

import arrow.core.Either
import arrow.core.right
import com.beust.klaxon.JsonObject
import org.geezer.json.Jsonable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.db
import com.materialcentral.scan.filter.ScanFindingFiltersTable
import org.geezer.db.schema.*
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.select

abstract class ScanFindingsTable<F : ScanFinding>(name: String) : DataTable<F>(name) {
    abstract fun mapFinding(finding: F, statement: FilteredUpdateStatement, insert: Boolean)

    val scanId = long("scan_id").referencesWithStandardNameAndIndex(ScansTable.id, ReferenceOption.CASCADE)

    val analyzerFamilyIds = stringList("analyzer_family_ids")

    val analyzeFindingSeverities = encodedJson("analyzer_severities", AnalyzerSeverities).nullable()

    val findingFilterId = long("filter_id").referencesWithStandardNameAndIndex(ScanFindingFiltersTable.id, ReferenceOption.SET_NULL).nullable()

    final override fun mapDataToStatement(finding: F, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[scanId] = finding.scanId
        statement[analyzerFamilyIds] = finding.analyzerIds
        statement[analyzeFindingSeverities] = if (finding.analyzerSeverities.isEmpty()) null else AnalyzerSeverities(finding.analyzerSeverities)
        statement[findingFilterId] = finding.scanFindingFilterId
        mapFinding(finding, statement, insert)
    }

    fun findScanFindings(scanId: Long): List<F> {
        return db {
            select { this@ScanFindingsTable.scanId eq scanId }.map(::constructData)
        }
    }
}

class AnalyzerSeverities(val data: Map<String, FindingSeverity> = mapOf()) : Jsonable {

    override fun toJson(): JsonObject {
        return JsonObject().apply {
            for ((type, severity) in data) {
                this[type] = severity.readableId
            }
        }
    }
    companion object : JsonObjectDecoder<AnalyzerSeverities> {
        override fun createDefault(): AnalyzerSeverities {
            return AnalyzerSeverities()
        }

        override fun decode(json: JsonObject, column: Column<*>, attributes: Map<String, Any>): Either<String, AnalyzerSeverities> {
            val data = mutableMapOf<String, FindingSeverity>()
            for (type in json.keys) {
                val severity = FindingSeverity.mapOptionalReadableId(json.string(type))
                if (severity != null) {
                    data[type] = severity
                }
            }

            return AnalyzerSeverities(data).right()
        }
    }
}