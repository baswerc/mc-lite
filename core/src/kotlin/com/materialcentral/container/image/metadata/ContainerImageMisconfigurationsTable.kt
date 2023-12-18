package com.materialcentral.container.image.metadata

import com.materialcentral.LocationsTable
import org.geezer.db.FilteredUpdateStatement
import org.geezer.db.schema.referencesWithStandardNameAndIndex
import com.materialcentral.scan.analysis.misconfiguration.MisconfigurationTypesTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow

object ContainerImageMisconfigurationsTable : ContainerImageMetadataTable<ContainerImageMisconfiguration>("container_image_misconfigurations") {

    val misconfigurationTypeId = long("misconfiguration_type_id").referencesWithStandardNameAndIndex(MisconfigurationTypesTable.id, ReferenceOption.CASCADE)

    override val filePathId = long("file_path_id").referencesWithStandardNameAndIndex(LocationsTable.id, ReferenceOption.NO_ACTION)

    override val metadataId: Column<Long> = misconfigurationTypeId

    override fun mapMetadata(metadata: ContainerImageMisconfiguration, statement: FilteredUpdateStatement, insert: Boolean) {
        statement[misconfigurationTypeId] = metadata.misconfigurationTypeId
        statement[filePathId] = metadata.filePathId
    }

    override fun constructData(row: ResultRow): ContainerImageMisconfiguration {
        return ContainerImageMisconfiguration(row[misconfigurationTypeId], row[filePathId], row[containerImageId], row[inheritedMetadata])
    }
}