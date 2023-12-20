package com.materialcentral.scan.file

import com.materialcentral.DataStringsTable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.scan.analysis.misconfiguration.MisconfigurationTypesTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object MisconfigurationScanFindingsTable : FileScanFindingsTable<MisconfigurationScanFinding>("misconfiguration_scan_findings") {

    val misconfigurationTypeId = long("misconfiguration_type_id").referencesWithStandardNameAndIndex(MisconfigurationTypesTable.id, ReferenceOption.CASCADE)

    val filePathId = long("file_path_id").referencesWithStandardNameAndIndex(DataStringsTable.id, ReferenceOption.NO_ACTION)

    val lineNumberStart = integer("line_number_start").nullable()

    val lineNumberEnd = integer("line_number_end").nullable()

    val resolutionId = long("resolution_id").referencesWithStandardNameAndIndex(DataStringsTable.id, ReferenceOption.NO_ACTION).nullable()

    override fun mapRepositoryFinding(misconfigurationFinding: MisconfigurationScanFinding, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[misconfigurationTypeId] = misconfigurationFinding.misconfigurationTypeId
        statement[filePathId] = misconfigurationFinding.filePathId
        statement[lineNumberStart] = misconfigurationFinding.lineNumberStart
        statement[lineNumberEnd] = misconfigurationFinding.lineNumberEnd
        statement[resolutionId] = misconfigurationFinding.resolutionId
    }

    override fun constructData(row: ResultRow): MisconfigurationScanFinding {
        return MisconfigurationScanFinding(row[misconfigurationTypeId], row[filePathId], row[lineNumberStart], row[lineNumberEnd], row[resolutionId], row[inheritedFinding],
            row[scanId], row[analyzerFamilyIds], row[analyzeFindingSeverities]?.data ?: mapOf(), row[findingFilterId])
    }
}