package com.materialcentral.schedule

import arrow.core.Either
import arrow.core.right
import com.beust.klaxon.JsonObject
import org.geezer.json.Jsonable
import org.geezer.db.schema.JsonObjectDecoder
import org.geezer.json.JsonableObject
import org.geezer.system.runtime.BooleanProperty
import org.geezer.system.runtime.RuntimeClock
import org.jetbrains.exposed.sql.Column
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class DaysOfWeek(val sunday: Boolean,
                 val monday: Boolean,
                 val tuesday: Boolean,
                 val wednesday: Boolean,
                 val thursday: Boolean,
                 val friday: Boolean,
                 val saturday: Boolean) : JsonableObject {

    constructor() : this(defaultSunday(), defaultMonday(), defaultTuesday(), defaultWednesday(), defaultThursday(), defaultFriday(), defaultSaturday())

    fun isRightDay(timestamp: Long = RuntimeClock.now): Boolean {
        val dayOfWeek = GregorianCalendar(RuntimeClock.timeZone).apply { timeInMillis = timestamp }.get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.SUNDAY -> sunday
            Calendar.MONDAY -> monday
            Calendar.TUESDAY -> tuesday
            Calendar.WEDNESDAY -> wednesday
            Calendar.THURSDAY -> thursday
            Calendar.FRIDAY -> friday
            Calendar.SATURDAY -> saturday
            else -> false
        }
    }

    override fun toJson(): JsonObject {
        return JsonObject().apply {
            this["sunday"] = sunday
            this["monday"] = monday
            this["tuesday"] = tuesday
            this["wednesday"] = wednesday
            this["thursday"] = thursday
            this["friday"] = friday
            this["saturday"] = saturday
        }
    }

    companion object : JsonObjectDecoder<DaysOfWeek> {
        val defaultSunday = BooleanProperty("DefaultScanDaysSunday", true)

        val defaultMonday = BooleanProperty("DefaultScanDaysMonday", true)

        val defaultTuesday = BooleanProperty("DefaultScanDaysTuesday", true)

        val defaultWednesday = BooleanProperty("DefaultScanDaysWednesday", true)

        val defaultThursday = BooleanProperty("DefaultScanDaysThursday", true)

        val defaultFriday = BooleanProperty("DefaultScanDaysFriday", true)

        val defaultSaturday = BooleanProperty("DefaultScanDaysSaturday", true)

        private val log: Logger = LoggerFactory.getLogger(javaClass)
        override fun createDefault(): DaysOfWeek {
            return DaysOfWeek()
        }

        fun map(json: JsonObject): Either<String, DaysOfWeek> {
            val sunday = json.boolean("sunday") ?: defaultSunday()
            val monday = json.boolean("monday") ?: defaultMonday()
            val tuesday = json.boolean("tuesday") ?: defaultTuesday()
            val wednesday = json.boolean("wednesday") ?: defaultWednesday()
            val thursday = json.boolean("thursday") ?: defaultThursday()
            val friday = json.boolean("friday") ?: defaultFriday()
            val saturday = json.boolean("saturday") ?: defaultSaturday()

            return DaysOfWeek(sunday, monday, tuesday, wednesday, thursday, friday, saturday).right()
        }

        override fun decode(json: JsonObject, column: Column<*>, attributes: Map<String, Any>): Either<String, DaysOfWeek> {
            return map(json)
        }

    }
}