package com.materialcentral.scan

import org.geezer.HasDescription
import org.geezer.db.ReadableDataEnum
import org.geezer.db.ReadableDataEnumType
import org.geezer.io.ui.FontIcon

enum class ScanMedium(override val id: Int, override val readableId: String, override val label: String, val icon: FontIcon, override val description: String?) : ReadableDataEnum, HasDescription {
    METADATA(0, "metadata", "Metadata", FontIcon("fa-file-circle-info", "e493"), "So feel been kept be at gate. Be september it extensive oh concluded of certainty. In read most gate at body held it ever no. Talking justice welcome message inquiry in started of am me. Led own hearted highest visited lasting sir through compass his."),
    ASSET(1, "asset", "Asset", FontIcon("fa-binary", "e33b"), "So feel been kept be at gate. Be september it extensive oh concluded of certainty. In read most gate at body held it ever no. Talking justice welcome message inquiry in started of am me. Led own hearted highest visited lasting sir through compass his.");

    companion object : ReadableDataEnumType<ScanMedium> {
        @JvmField
        val Icon = FontIcon("fa-box-archive", "f187")

        override val dataEnumValues: Array<ScanMedium> = values()

    }
}