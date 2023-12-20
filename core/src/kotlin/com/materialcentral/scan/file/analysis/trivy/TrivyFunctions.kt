package com.materialcentral.scan.file.analysis.trivy

import com.materialcentral.scan.FindingSeverity

fun trivySeverity(severity: String?): FindingSeverity? {
    return when (severity?.uppercase()) {
        "CRITICAL" -> FindingSeverity.CRITICAL
        "HIGH" -> FindingSeverity.HIGH
        "MEDIUM" -> FindingSeverity.MEDIUM
        "LOW" -> FindingSeverity.LOW
        "UNKNOWN" -> FindingSeverity.UNKNOWN
        else -> null
    }
}