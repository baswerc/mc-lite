package com.materialcentral.scan.file.analysis.trivy

enum class TrivyMisconfStatus(val id: String) {
    PASS("PASS"),
    FAIL("FAIL"),
    EXCEPTION("EXCEPTION");

    companion object {
        fun fromId(id: String?): TrivyMisconfStatus? = values().firstOrNull { it.id.equals(id, true) }
    }
}