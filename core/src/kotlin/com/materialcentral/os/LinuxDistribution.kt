package com.materialcentral.os

import org.geezer.db.DataEnum
import org.geezer.db.DataEnumType
import com.materialcentral.oss.PackageType

enum class LinuxDistribution(override val id: Int, override val label: String, val sbomIdentifiers: List<String>, val packageType: PackageType) : DataEnum {
    ALPINE(0, "Alpine", listOf("alpine"), PackageType.APK),
    AMAZON(1, "Amazon Linux", listOf("amazon_linux"), PackageType.RPM),
    DEBIAN(2, "Debian", listOf("debian"), PackageType.DEB),
    PHOTON(3, "Photon", listOf("photon"), PackageType.RPM),
    REDHAT(4, "Redhat", listOf("redhat"), PackageType.RPM),
    CENTOS(5, "CentOS", listOf("centos"), PackageType.RPM),
    ROCKY(6, "Rock Linux", listOf("rocky_linux"), PackageType.RPM),
    ALMA(7, "Alma Linux", listOf("alma_linux"), PackageType.RPM),
    ORACLE(8, "Oracle Linux", listOf("oracle_linux"), PackageType.RPM),
    OPEN_SUSE(9, "Open SUSE", listOf("open_suse"), PackageType.RPM),
    ENTERPRISE_SUSE(10, "Enterprise SUSE", listOf("enterprise_suse"), PackageType.RPM),
    UBUNTU(11, "Ubuntu", listOf("ubuntu"), PackageType.DEB),
    FEDORA(12, "Fedora", listOf("fedora"), PackageType.RPM),
    GENTO(13, "Gento", listOf("gento"), PackageType.GENERIC),
    CBL_MARINER(14, "CBL-Mariner", listOf("cbl_mariner"), PackageType.RPM),
    WOLFI(15, "Wolfi", listOf("wolfi"), PackageType.APK),
    ;

    companion object : DataEnumType<LinuxDistribution> {
        override val dataEnumValues: Array<LinuxDistribution> = values()

        fun mapSbomIdentifier(id: String?): LinuxDistribution? = id?.let { id -> values().firstOrNull { it.sbomIdentifiers.contains(id) } }

        fun fromPackageType(packageType: PackageType): LinuxDistribution? = values().firstOrNull { it.packageType == packageType }
    }
}