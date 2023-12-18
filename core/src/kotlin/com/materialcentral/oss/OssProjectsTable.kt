package com.materialcentral.oss

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.enum
import com.materialcentral.oss.host.OpenSourceProjectHost
import org.jetbrains.exposed.sql.ResultRow

object OssProjectsTable : DataTable<OssProject>("oss_projects") {
    val host = enum("host_id", OpenSourceProjectHost)
    
    val organization = varchar("organization", 100)

    val repository = varchar("repository", 100)

    val description = description()
    
    val license = enum("license_id",  OSSLicense).nullable()
    
    val public = bool("public").nullable()
    
    val stars = integer("stars").nullable()
    
    val subscribers = integer("subscribers").nullable()

    val watchers = integer("watchers").nullable()

    val forks = integer("forks").nullable()

    val contributors = integer("contributors").nullable()

    val openIssues = integer("open_issues").nullable()

    val archived = bool("archived").nullable()

    val disabled = bool("disabled").nullable()

    val createdAt = createdAt().nullable()

    val lastCommitAt = long("last_commit_at").nullable()
    
    override fun mapDataToStatement(project: OssProject, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[host] = project.host
        statement[organization] = project.organization
        statement[repository] = project.repository
        statement[description] = project.description
        statement[license] = project.license
        statement[public] = project.public
        statement[stars] = project.stars
        statement[subscribers] = project.subscribers
        statement[watchers] = project.watchers
        statement[forks] = project.forks
        statement[contributors] = project.contributors
        statement[openIssues] = project.openIssues
        statement[archived] = project.archived
        statement[disabled] = project.disabled
        statement[createdAt] = project.createdAt
        statement[lastCommitAt] = project.lastCommitAt
    }

    override fun constructData(row: ResultRow): OssProject {
        return OssProject(row[host], row[organization], row[repository], row[description], row[license], row[public], row[stars], row[subscribers], row[watchers], row[forks], row[contributors],
            row[openIssues], row[archived], row[disabled], row[createdAt], row[lastCommitAt])
    }
}