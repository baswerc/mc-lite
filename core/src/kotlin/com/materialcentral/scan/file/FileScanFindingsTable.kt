package com.materialcentral.scan.file

import org.geezer.db.FilteredUpdateStatement
import com.materialcentral.scan.ScanFindingsTable

abstract class FileScanFindingsTable<R : FileScanFinding>(name: String) : ScanFindingsTable<R>(name) {

    abstract fun mapRepositoryFinding(repositoryFinding: R, statement: FilteredUpdateStatement, insert: Boolean)

    val inheritedFinding = bool("inherited_finding")

    final override fun mapFinding(repositoryFinding: R, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[inheritedFinding] = repositoryFinding.inheritedFinding
        mapRepositoryFinding(repositoryFinding, statement, insert)
    }
}