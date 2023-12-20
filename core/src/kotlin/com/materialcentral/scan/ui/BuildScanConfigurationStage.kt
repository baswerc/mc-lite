package com.materialcentral.scan.ui

enum class BuildScanConfigurationStage(val id: Int, val label: String) {
    INITIAL_CONFIGURATION(0, "Configure Schedule"),
    CONFIGURE_SCAN_MEDIUM(1, "Configure Scan Medium"),
    SELECT_ANALYZERS(2, "Select Analyzers"),
    CONFIGURE_ANALYSIS(3, "Configure Analysis"),
    CONFIGURE_FINDING_TYPE(4, "Configure Finding Type"),
    CONFIRM_CONFIGURATION(5, "Confirm Configuration");

    companion object {
        fun map(id: Int?): BuildScanConfigurationStage? {
            return values().firstOrNull { it.id == id }
        }
    }
}