package com.materialcentral.scan.schedule

import com.materialcentral.CandidateMatchType
import org.geezer.db.Data
import org.geezer.io.ui.FontIcon
import org.geezer.io.ui.Linkable
import com.materialcentral.scan.ScanConfiguration
import com.materialcentral.scan.ScanMedium
import com.materialcentral.scan.ScanTargetSourceType
import com.materialcentral.scan.schedule.ui.ScanScheduleUiController
import org.geezer.io.ui.HasNameDescriptionIcon
import org.geezer.system.runtime.IntProperty
import org.geezer.system.runtime.RuntimeClock
import kotlin.reflect.KFunction

class ScanSchedule(
    override var name: String,
    override val description: String?,
    var active: Boolean,
    var candidateMatchType: CandidateMatchType,
    var scanTargetSourceTypes: List<ScanTargetSourceType>,
    var medium: ScanMedium,
    var scanConfiguration: ScanConfiguration,
    var contributeToTargetMetadata: Boolean,
    var minimumHoursBetweenScans: Int?,
    var scanTimeRange: ScanTimeRange?,
    var scanDays: ScanDays?,
    val scanAllTargets: Boolean, // container images, code branches
    val scanDefaultTarget: Boolean, // latest container image, default code branch
    val scanTargetNamePatterns: List<String>, // container image tag name pattern, code branch tag name patterns
    val scanTargetInEnvironmentIds: List<Long>, // container images deployed to environments
    val scanNewTargetsImmediately: Boolean // scan new container images immediately?
    ) : Data(), HasNameDescriptionIcon, Linkable {

    override val icon: FontIcon = Icon

    override val route: KFunction<*> = Route

    fun isRightTimeAndDay(timestamp: Long = RuntimeClock.now): Boolean {
        return scanDays?.isRightDay(timestamp) != false
    }

    companion object {
        val defaultMinHoursBetweenScans = IntProperty("ScansDefaultMinHoursBetween", 48, minValue = 1)

        @JvmField
        val Icon = FontIcon("fa-calendar-clock", "e0d2")

        @JvmField
        val Route = ScanScheduleUiController::get
    }
}