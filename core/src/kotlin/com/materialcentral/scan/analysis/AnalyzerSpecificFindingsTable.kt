package com.materialcentral.scan.analysis

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.enum
import org.geezer.db.schema.tableUniqueConstraint
import com.materialcentral.scan.FindingSeverity
import org.geezer.db.schema.stringList
import org.jetbrains.exposed.sql.and

abstract class AnalyzerSpecificFindingsTable<F : AnalyzerSpecificFinding>(name: String) : DataTable<F>(name) {
    abstract fun mapFinding(finding: F, statement: FilteredUpdateStatement, insert: Boolean)

    val analyzerToolId = varchar("analyzer_tool_id", 100)

    val identifier = varchar("identifiers", 100)

    val severity = enum("analyzer_severity_id", FindingSeverity)

    val title = varchar("title", 500).nullable()

    val description = description()

    val detailsUrls = stringList("details_urls")

    init {
        tableUniqueConstraint(analyzerToolId, identifier)
    }

    final override fun mapDataToStatement(finding: F, statement: FilteredUpdateStatement, insert: Boolean) {
        if (insert) {
            statement[analyzerToolId] = finding.analyzerToolId
            statement[identifier] = finding.identifier
        }
        statement[severity] = finding.severity
        statement[title] = finding.title
        statement[description] = finding.description
        statement[detailsUrls] = finding.detailsUrls
    }

    fun find(analyzerToolId: String, identifier: String): F? {
        return findUniqueWhere { (this@AnalyzerSpecificFindingsTable.analyzerToolId eq analyzerToolId) and (this@AnalyzerSpecificFindingsTable.identifier eq identifier) }
    }

    fun updateIfNecessary(finding: F, severity: FindingSeverity, title: String?, description: String?, detailsUrls: List<String>): F {
        var update = false
        if (finding.severity != severity) {
            finding.severity = severity
            update = true
        }

        if (finding.title != title) {
            finding.title = title
            update = true
        }

        if (finding.description != description) {
            finding.description = description
            update = true
        }

        if (finding.detailsUrls != detailsUrls) {
            finding.detailsUrls = detailsUrls
            update = true
        }

        if (update) {
            update(finding)
        }

        return finding
    }
}