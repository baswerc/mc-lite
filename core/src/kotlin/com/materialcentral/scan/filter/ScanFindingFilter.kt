package com.materialcentral.scan.filter

import com.materialcentral.CandidateMatchType
import com.materialcentral.scan.FindingType
import com.materialcentral.scan.filter.ui.ScanFindingFiltersUiController
import org.geezer.db.Data
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.HasNameDescriptionIcon
import org.geezer.io.ui.Linkable
import org.geezer.toRegexOrNull
import kotlin.reflect.KFunction

class ScanFindingFilter(
    override var name: String,
    override var description: String?,
    var active: Boolean,
    var candidateMatchType: CandidateMatchType,
    var findingType: FindingType,
    var reason: ScanFindingFilterReason,
    var findingPrimaryIdentifierPatterns: List<String>, // Finding identifier, PURL
    var findingSecondaryIdentifierPatterns: List<String>, // PURL (For vulnerabilities)
    var locationPatterns: List<String>,
    val createdAt: Long
) : Data(), HasNameDescriptionIcon, Linkable {

    override val icon: FontIcon = Icon

    override val route: KFunction<*> = Route

    val findingPrimaryIdentifierRegexs: List<Regex> by lazy { findingPrimaryIdentifierPatterns.mapNotNull { it.toRegexOrNull() } }

    val findingSecondaryIdentifierRegexs: List<Regex> by lazy { findingSecondaryIdentifierPatterns.mapNotNull { it.toRegexOrNull() } }

    val locationRegexs: List<Regex> by lazy { locationPatterns.mapNotNull { it.toRegexOrNull() } }

    companion object {
        @JvmField
        val Icon = FontIcon("fa-filter", "f0b0")

        @JvmField
        val Route = ScanFindingFiltersUiController::getFilter
    }
}