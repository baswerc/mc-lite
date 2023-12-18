package com.materialcentral.container.image.metadata

import com.materialcentral.LocationsTable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.scan.analysis.secret.SecretTypesTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object ContainerImageSecretsTable : ContainerImageMetadataTable<ContainerImageSecret>("container_image_secrets") {

    val secretTypeId = long("secret_type_id").referencesWithStandardNameAndIndex(SecretTypesTable.id, ReferenceOption.CASCADE)

    override val filePathId = long("file_path_id").referencesWithStandardNameAndIndex(LocationsTable.id, ReferenceOption.NO_ACTION)

    override val metadataId: Column<Long> = secretTypeId

    override fun mapMetadata(metadata: ContainerImageSecret, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[secretTypeId] = metadata.secretTypeId
        statement[filePathId] = metadata.filePathId
    }

    override fun constructData(row: ResultRow): ContainerImageSecret {
        return ContainerImageSecret(row[secretTypeId], row[filePathId], row[containerImageId], row[inheritedMetadata])
    }
}