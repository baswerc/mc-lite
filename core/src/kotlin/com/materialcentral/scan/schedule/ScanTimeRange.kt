package com.materialcentral.scan.schedule

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.beust.klaxon.JsonObject
import org.geezer.json.Jsonable
import org.geezer.db.schema.JsonObjectDecoder
import org.geezer.system.runtime.IntProperty
import org.geezer.system.runtime.RuntimeClock
import org.jetbrains.exposed.sql.Column
import java.util.*

class ScanTimeRange(
    val fromHour: Int,
    val fromMinute: Int,
    val toHour: Int,
    val toMinute: Int) : Jsonable {

    constructor() : this(defaultFromHours(), defaultFromMinutes(), defaultToHours(), defaultToMinutes())

    fun isRightTime(timestamp: Long): Boolean {
        val calendar = GregorianCalendar(RuntimeClock.timeZone).apply { timeInMillis = timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return if (hour < fromHour) {
            false
        } else if (hour == fromHour && minute < fromMinute) {
            false
        } else if (hour > toHour) {
            false
        } else if (hour == toHour && minute > toMinute) {
            false
        } else {
            true
        }
    }

    override fun toJson(): JsonObject {
        return JsonObject().apply {
            this["fromHour"] = fromHour
            this["fromMinute"] = fromMinute
            this["toHour"] = toHour
            this["toMinute"] = toMinute
        }
    }

    companion object : JsonObjectDecoder<ScanTimeRange> {
        val defaultFromHours = IntProperty("ScanScheduleFromHourDefault", 0)

        val defaultFromMinutes = IntProperty("ScanScheduleFromMinuteDefault", 0)

        val defaultToHours = IntProperty("ScanScheduleFromHourDefault", 23)

        val defaultToMinutes = IntProperty("ScanScheduleFromMinuteDefault", 59)
        override fun createDefault(): ScanTimeRange {
            return ScanTimeRange()
        }

        override fun decode(json: JsonObject, column: Column<*>, attributes: Map<String, Any>): Either<String, ScanTimeRange> {
            return map(json)
        }

        fun map(json: JsonObject): Either<String, ScanTimeRange> {
            var fromHour = json.int("fromHour")
            if (fromHour == null) {
                return "Missing fromHour property.".left()
            }

            var fromMinute = json.int("fromMinute")
            if (fromMinute == null) {
                return "Missing fromMinute property.".left()
            }

            var toHour = json.int("toHour")
            if (toHour == null) {
                return "Missing toHour property.".left()
            }

            var toMinute = json.int("toMinute")
            if (toMinute == null) {
                return "Missing toMinute property.".left()
            }

            return ScanTimeRange(fromHour, fromMinute, toHour, toMinute).right()
        }
    }
}