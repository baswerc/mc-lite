package com.materialcentral.oss.host.github

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.secret.SecretsTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object GitHubAccessesTable : DataTable<GitHubAccess>("github_accesses") {
    val apiUserName = varchar("api_user_name", 500)

    val apiUserTokenSecretId = long("api_user_token_secret_id").referencesWithStandardNameAndIndex(SecretsTable.id, ReferenceOption.SET_NULL).nullable()

    val active = bool("active")

    override fun mapDataToStatement(gitHubAccess: GitHubAccess, statement: FilteredUpdateStatement, insert: Boolean) {
       statement[apiUserName] = gitHubAccess.apiUserName
        statement[apiUserTokenSecretId] = gitHubAccess.apiUserTokenSecretId
        statement[active] = gitHubAccess.active
    }

    override fun constructData(row: ResultRow): GitHubAccess {
        return GitHubAccess(row[apiUserName], row[apiUserTokenSecretId], row[active])
    }

}