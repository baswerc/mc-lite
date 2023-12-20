package com.materialcentral.scan

import org.geezer.HasDescription
import org.geezer.HasReadableId
import org.geezer.db.DataEnum
import org.geezer.db.ReadableDataEnumType
import org.geezer.io.ui.FontIcon

enum class FindingSeverity(override val id: Int, override val label: String, override val readableId: String, val icon: FontIcon, override val description: String?) : DataEnum, HasReadableId, HasDescription {
    UNKNOWN(0, "Unknown", "unknown", FontIcon("fa-circle-question", "f059"), "The severity level of the finding is unknown."),
    INFO(1, "Informational", "info", FontIcon("fa-circle-info", "f05a"), "Information not related to a potential threat."),
    LOW(2, "Low", "low", FontIcon("fa-square-exclamation", "f321"), "These are the types of findings that are believed to require unlikely circumstances to be able to be exploited, or where a successful exploit would give minimal consequence.s"),
    MEDIUM(3, "Medium", "medium", FontIcon("fa-triangle-exclamation", "f071"), "This rating is given to findings that may be more difficult to exploit but could still lead to some compromise of the confidentiality, integrity or availability of resources under certain circumstances."),
    HIGH(4, "High", "high", FontIcon("fa-shield-exclamation", "e247"), "This rating is given to findings that can easily compromise the confidentiality, integrity or availability of resources."),
    CRITICAL(5, "Critical", "critical", FontIcon("fa-circle-radiation", "f7ba"), "This rating is given to findings that could be easily exploited by a remote unauthenticated attacker and lead to system compromise (arbitrary code execution) without requiring user interaction.");

    companion object : ReadableDataEnumType<FindingSeverity> {
        override val dataEnumValues: Array<FindingSeverity> = values()
    }
}