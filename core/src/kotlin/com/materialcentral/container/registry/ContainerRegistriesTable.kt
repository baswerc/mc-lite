package com.materialcentral.container.registry

import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.DataTable
import org.geezer.db.schema.enum
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import org.geezer.db.schema.uniqueIndexWithStandardName
import com.materialcentral.secret.SecretsTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object ContainerRegistriesTable : DataTable<ContainerRegistry>("container_registries") {

    val type = enum("type_id", ContainerRegistryType)

    val hostname = varchar("hostname", 500).uniqueIndexWithStandardName()

    val ssl = bool("ssl")

    val description = description()

    val active = active()

    val authenticationType = enum("authentication_type_id", RegistryAuthenticationType)

    val secretOneId = long("secret_one_id").referencesWithStandardNameAndIndex(SecretsTable.id, ReferenceOption.SET_NULL).nullable()

    val secretTwoId = long("secret_two_id").referencesWithStandardNameAndIndex(SecretsTable.id, ReferenceOption.SET_NULL).nullable()

    override fun mapDataToStatement(registry: ContainerRegistry, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[type] = registry.type
        statement[hostname] = registry.hostname
        statement[ssl] = registry.ssl
        statement[description] = registry.description
        statement[active] = registry.active
        statement[authenticationType] = registry.authenticationType
        statement[secretOneId] = registry.secretOneId
        statement[secretTwoId] = registry.secretTwoId
    }

    override fun constructData(row: ResultRow): ContainerRegistry {
        return ContainerRegistry(row[type], row[hostname], row[ssl], row[description], row[active], row[authenticationType], row[secretOneId], row[secretTwoId])
    }
}