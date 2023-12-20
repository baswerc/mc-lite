package com.materialcentral.scan.file.analysis.trivy

/**
 * https://github.com/aquasecurity/trivy/blob/main/pkg/types/report.go#L44
 */
enum class TrivyResultClass(val id: String) {
    OS_PACKAGES("os-pkgs"),
    LANGUAGE_PACKAGES("lang-pkgs"),
    CONFIG("config"),
    SECRET("secret"),
    LICENSE("license"),
    LICENSE_FILE("license-file"),
    CUSTOM("custom");

    companion object {
        fun fromId(id: String?): TrivyResultClass? = values().firstOrNull { it.id == id }
    }

}