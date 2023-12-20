package com.materialcentral.scan.file.analysis.trivy

import com.materialcentral.os.LinuxDistribution
import com.materialcentral.os.OperatingSystemType

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/fanal/analyzer/os/const.go#L7
 */
enum class TrivyOsFamily(val id: String, val os: OperatingSystemType, val linuxDistribution: LinuxDistribution?) {
    REDHAT("redhat", OperatingSystemType.LINUX, LinuxDistribution.REDHAT),
    DEBIAN("debian", OperatingSystemType.LINUX, LinuxDistribution.DEBIAN),
    UBUNTU("ubuntu", OperatingSystemType.LINUX, LinuxDistribution.UBUNTU),
    CENTOS("centos", OperatingSystemType.LINUX, LinuxDistribution.CENTOS),
    ROCKY("rocky", OperatingSystemType.LINUX, LinuxDistribution.ROCKY),
    ALMA("alma", OperatingSystemType.LINUX, LinuxDistribution.ALMA),
    FEDORA("fedora", OperatingSystemType.LINUX, LinuxDistribution.FEDORA),
    AMAZON("amazon", OperatingSystemType.LINUX, LinuxDistribution.AMAZON),
    ORACLE("oracle", OperatingSystemType.LINUX, LinuxDistribution.ORACLE),
    CBL_MARINER("cbl-mariner", OperatingSystemType.LINUX, LinuxDistribution.CBL_MARINER),
    WINDOWS("windows", OperatingSystemType.WINDOWS, null),
    OPEN_SUSE("opensuse", OperatingSystemType.LINUX, LinuxDistribution.OPEN_SUSE),
    OPEN_SUSE_LEAP("opensuse.leap", OperatingSystemType.LINUX, LinuxDistribution.OPEN_SUSE),
    OPEN_SUSE_TUMBLEWEED("opensuse.tumbleweed", OperatingSystemType.LINUX, LinuxDistribution.OPEN_SUSE),
    ENTERPRISE_SUSE("suse linux enterprise server", OperatingSystemType.LINUX, LinuxDistribution.ENTERPRISE_SUSE),
    PHOTO("photon", OperatingSystemType.LINUX, LinuxDistribution.PHOTON),
    ALPINE("alpine", OperatingSystemType.LINUX, LinuxDistribution.ALPINE),
    WOLFI("wolfi", OperatingSystemType.LINUX, LinuxDistribution.WOLFI),
    CHAINGUARD("chainguard", OperatingSystemType.LINUX, LinuxDistribution.WOLFI);

    companion object {
        fun map(id: String?): TrivyOsFamily? = values().firstOrNull { it.id.equals(id, true) }
    }
}