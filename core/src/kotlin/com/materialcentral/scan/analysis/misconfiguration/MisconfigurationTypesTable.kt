package com.materialcentral.scan.analysis.misconfiguration

import org.geezer.db.FilteredUpdateStatement
import com.materialcentral.scan.FindingSeverity
import com.materialcentral.scan.analysis.AnalyzerSpecificFindingsTable
import org.jetbrains.exposed.sql.ResultRow

object MisconfigurationTypesTable : AnalyzerSpecificFindingsTable<MisconfigurationType>("misconfiguration_types") {
    override fun mapFinding(finding: MisconfigurationType, statement: FilteredUpdateStatement, insert: Boolean) {}

    override fun constructData(row: ResultRow): MisconfigurationType {
        return MisconfigurationType(row[analyzerToolId], row[identifier],row[severity], row[title], row[description], row[detailsUrls])
    }

    fun findUpdateOrCreate(analyzerToolId: String, identifier: String, severity: FindingSeverity, title: String?, description: String?, detailsUrls: List<String>): MisconfigurationType {
        val misconfigurationType = find(analyzerToolId, identifier)
        return if (misconfigurationType != null) {
            updateIfNecessary(misconfigurationType, severity, title, description, detailsUrls)
        } else {
            try {
                MisconfigurationTypesTable.create(MisconfigurationType(analyzerToolId, identifier, severity, title, description, detailsUrls))
            } catch (e: Exception) {
                find(analyzerToolId, identifier) ?: throw e
            }
        }
    }

}