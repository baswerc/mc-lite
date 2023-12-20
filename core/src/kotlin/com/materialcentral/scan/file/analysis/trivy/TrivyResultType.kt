package com.materialcentral.scan.file.analysis.trivy

import com.materialcentral.os.LinuxDistribution
import com.materialcentral.oss.PackageType

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/fanal/analyzer/const.go#L11
 */
enum class TrivyResultType(val id: String, val packageType: PackageType? = null, val linuxDistribution: LinuxDistribution? = null) {
    // OS
    OS_RELEASE("os-release", null),
    ALPINE("alpine", PackageType.ALPINE, LinuxDistribution.ALPINE),
    AMAZAON("amazon", PackageType.RPM, LinuxDistribution.AMAZON),
    CBL_MARINER("cbl-mariner", PackageType.RPM, LinuxDistribution.CBL_MARINER),
    DEBINA("debian", PackageType.DEB),
    PHOTON("photon", PackageType.RPM, LinuxDistribution.PHOTON),
    CENTOS("centos", PackageType.RPM, LinuxDistribution.CENTOS),
    ROCKY("rocky", PackageType.RPM, LinuxDistribution.ROCKY),
    ALMA("alma", PackageType.RPM, LinuxDistribution.ALMA),
    FEDORA("fedora", PackageType.RPM, LinuxDistribution.FEDORA),
    ORACLE("oracle", PackageType.RPM, LinuxDistribution.ORACLE),
    REDHAT("redhat", PackageType.RPM, LinuxDistribution.REDHAT),
    SUSE("suse", PackageType.RPM, LinuxDistribution.ENTERPRISE_SUSE),
    OPEN_SUSE("opensuse", PackageType.RPM, LinuxDistribution.OPEN_SUSE),
    OPEN_SUSE_LEAP("opensuse.leap", PackageType.RPM, LinuxDistribution.OPEN_SUSE),
    OPEN_SUSE_TUMBLEWEED("opensuse.tumbleweed", PackageType.RPM, LinuxDistribution.OPEN_SUSE),
    UBUNTU("ubuntu", PackageType.DEB, LinuxDistribution.UBUNTU),

    // OS Package
    APK("apk", PackageType.APK),
    DPKG("dpkg", PackageType.DEB),
    RPM("rpm", PackageType.RPM),
    RPMQA("rpmqa", PackageType.RPM),

    // OS Package Repository
    APK_REPO("apk-repo"),


    // Ruby
    BUNDLER("bundler", PackageType.RUBY_GEM),
    GEMSPEC("gemspec", PackageType.RUBY_GEM),

    // Rust
    CARGO("cargo", PackageType.CARGO),

    // PHP
    PHP("composer", PackageType.COMPOSER),

    // Java
    JAR("jar", PackageType.MAVEN_MODULE),
    POM("pom", PackageType.MAVEN_MODULE),

    // Node.js
    NPM("npm", PackageType.NPM_MODULE),
    NODE_PKG("node-pkg", PackageType.NPM_MODULE),
    YARN("yarn", PackageType.NPM_MODULE),

    // .NET
    NUGET("nuget", PackageType.NUGET),

    // Python
    PYTHON_PKG("python-pkg", PackageType.PYPI),
    PIP("pip", PackageType.PYPI),
    PIPENV("pipenv", PackageType.PYPI),
    POETRY("poetry", PackageType.PYPI),

    // Go
    GOBINARY("gobinary", PackageType.GOLANG),
    GOMOD("gomod", PackageType.GOLANG),

    // Image Config
    APK_COMMAND("apk-command"),

    // Structured Config
    YAML("yaml"),
    JSON("json"),
    DOCKERFILE("dockerfile"),
    TERRAFORM("terraform"),
    CLOUD_FORMATION("cloudFormation"),
    HELM("helm"),
    KUBERNETES("kubernetes"),

    // Secrets
    SECRET("secret"),

    // =======
    // Red Hat
    // =======
    REDHAT_MANIFEST("redhat-content-manifest"),
    REDHAT_DOCKERFILE("redhat-dockerfile"),

    ;

    companion object {
        fun fromId(id: String?): TrivyResultType? = values().firstOrNull { it.id == id }
    }

}