package com.materialcentral.scan.analysis.secret

import org.geezer.db.FilteredUpdateStatement
import com.materialcentral.scan.FindingSeverity
import com.materialcentral.scan.analysis.AnalyzerSpecificFindingsTable
import org.jetbrains.exposed.sql.ResultRow

object SecretTypesTable : AnalyzerSpecificFindingsTable<SecretType>("secret_types") {
    override fun mapFinding(finding: SecretType, statement: FilteredUpdateStatement, insert: Boolean) {}

    override fun constructData(row: ResultRow): SecretType {
        return SecretType(row[analyzerToolId], row[identifier], row[severity],  row[title], row[description], row[detailsUrls])
    }

    fun findUpdateOrCreate(analyzerToolId: String, identifier: String, severity: FindingSeverity, title: String?, description: String?, detailsUrls: List<String>): SecretType {
        val secretType = find(analyzerToolId, identifier)
        return if (secretType != null) {
            updateIfNecessary(secretType, severity, title, description, detailsUrls)
        } else {
            try {
                create(SecretType(analyzerToolId, identifier, severity, title, description, detailsUrls))
            } catch (e: Exception) {
                find(analyzerToolId, identifier) ?: throw e
            }
        }
    }
}