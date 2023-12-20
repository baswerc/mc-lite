package com.materialcentral.scan

import org.geezer.HasDescription
import org.geezer.HasReadableId
import org.geezer.db.DataEnum
import org.geezer.db.ReadableDataEnumType
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.HasIcon
import com.materialcentral.oss.OssPackage
import com.materialcentral.scan.analysis.misconfiguration.MisconfigurationType
import com.materialcentral.scan.analysis.secret.SecretType
import com.materialcentral.vulnerability.KnownVulnerability

enum class FindingType(
    override val id: Int,
    override val readableId: String,
    override val label: String,
    val pluralLabel: String,
    val medianSources: Set<ScanMedium>,
    val analyzerUnique: Boolean,
    val hasSeverities: Boolean,
    override val icon: FontIcon,
    override val description: String

    ) : DataEnum, HasReadableId, HasDescription, HasIcon {

    OSS_PACKAGE_RELEASE(0, "oss-package", "OSS Package", "OSS Packages", setOf(ScanMedium.ASSET), false, false, OssPackage.Icon, "Are own design entire former get should. Advantages boisterous day excellence boy. Out between our two waiting wishing. Pursuit he he garrets greater towards amiable so placing. Nothing off how norland delight.") {
    },
    KNOWN_VULNERABILITY(1, "known-vulnerability", "Known Vulnerability", "Known Vulnerabilities", setOf(ScanMedium.ASSET, ScanMedium.METADATA), false, true, KnownVulnerability.Icon, "Are own design entire former get should. Advantages boisterous day excellence boy. Out between our two waiting wishing. Pursuit he he garrets greater towards amiable so placing. Nothing off how norland delight.") {
    },
    SECRET(2, "secret", "Secret", "Secrets", setOf(ScanMedium.ASSET), true, true, SecretType.Icon, "Are own design entire former get should. Advantages boisterous day excellence boy. Out between our two waiting wishing. Pursuit he he garrets greater towards amiable so placing. Nothing off how norland delight.") {
    },
    MISCONFIGURATION(3, "misconfiguration", "Misconfiguration", "Misconfigurations", setOf(ScanMedium.ASSET), true, true, MisconfigurationType.Icon, "Are own design entire former get should. Advantages boisterous day excellence boy. Out between our two waiting wishing. Pursuit he he garrets greater towards amiable so placing. Nothing off how norland delight.") {
    };

    companion object : ReadableDataEnumType<FindingType> {
        val repositoryFindingTypes = setOf(OSS_PACKAGE_RELEASE, KNOWN_VULNERABILITY, SECRET, MISCONFIGURATION)

        override val dataEnumValues: Array<FindingType> = values()
    }
}