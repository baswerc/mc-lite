package com.materialcentral.scan.file.analysis.trivy

/**
 * https://github.com/aquasecurity/fanal/blob/main/types/const.go
 * https://pkg.go.dev/github.com/aquasecurity/fanal/types
 */
enum class TrivyFileType(val trivyTypeId: String) {
    Bundler("bundler"),
    Cargo("cargo"),
    Composer("composer"),
    Npm("npm"),
    NuGet("nuget"),
    Pipenv("pipenv"),
    Poetry("poetry"),
    Yarn("yarn"),
    Jar("jar"),
    GoBinary("gobinary"),
    GoMod("gomod"),
    YAML("yaml"),
    JSON("json"),
    TOML("toml"),
    Dockerfile("dockerfile"),
    HCL("hcl"),
    Kubernetes("kubernetes"),
    CloudFormation("cloudformation"),
    Ansible("ansible");

    companion object {
        fun findFromType(typeId: String?): TrivyFileType? = values().firstOrNull { it.trivyTypeId == typeId }
    }
}
