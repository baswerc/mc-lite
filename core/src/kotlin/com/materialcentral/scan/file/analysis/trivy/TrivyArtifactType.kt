package com.materialcentral.scan.file.analysis.trivy

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/fanal/types/artifact.go#L188
 */
enum class TrivyArtifactType(val id: String) {
    CONTAINER_IMAGE("container_image"),
    FILESYSTEM("filesystem"),
    REPOSITORY("repository"),
    CYCLONEDX("cyclonedx"),
    SPDX("spdx"),
    AWS_ACCOUNT("aws_account"),
    VM("vm");

    companion object {
        fun fromId(id: String?): TrivyArtifactType? = values().firstOrNull { it.id.equals(id, true) }
    }
}