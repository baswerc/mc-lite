package com.materialcentral.scan.file.analysis.trivy

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/fanal/types/license.go#L11
 */
enum class TrivyLicenseCategory(val id: String) {
    FORBIDDEN("forbidden"),
    RESTRICTED("restricted"),
    RECIPROCAL("reciprocal"),
    NOTICE("notice"),
    PERMISSIVE("permissive"),
    UNENCUMBERED("unencumbered"),
    UNKNOWN("unknown");

    companion object {
        fun map(id: String?): TrivyLicenseCategory? = values().firstOrNull { it.id == id }
    }
}