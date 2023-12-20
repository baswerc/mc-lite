package com.materialcentral.scan.file

import com.materialcentral.DataStringsTable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.scan.analysis.secret.SecretTypesTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object SecretScanFindingsTable : FileScanFindingsTable<SecretScanFinding>("secret_scan_findings") {

    val secretTypeId = long("secret_type_id").referencesWithStandardNameAndIndex(SecretTypesTable.id, ReferenceOption.CASCADE)

    val filePathId = long("file_path_id").referencesWithStandardNameAndIndex(DataStringsTable.id, ReferenceOption.NO_ACTION)

    val lineNumberStart = integer("line_number_start").nullable()

    val lineNumberEnd = integer("line_number_end").nullable()

    override fun mapRepositoryFinding(secretFinding: SecretScanFinding, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[secretTypeId] = secretFinding.secretTypeId
        statement[filePathId] = secretFinding.filePathId
        statement[lineNumberStart] = secretFinding.lineNumberStart
        statement[lineNumberEnd] = secretFinding.lineNumberEnd
    }

    override fun constructData(row: ResultRow): SecretScanFinding {
        return SecretScanFinding(row[secretTypeId], row[filePathId], row[lineNumberStart], row[lineNumberEnd], row[inheritedFinding], row[scanId], row[analyzerFamilyIds],
            row[analyzeFindingSeverities]?.data ?: mapOf(), row[findingFilterId])
    }
}