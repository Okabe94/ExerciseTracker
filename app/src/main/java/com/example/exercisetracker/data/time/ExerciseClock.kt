@file:OptIn(ExperimentalTime::class)

package com.example.exercisetracker.data.time

import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.time.AppClock
import com.example.exercisetracker.domain.timezone.AppTimeZone
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime

class ExerciseClock(
    private val timeZone: AppTimeZone
) : AppClock {

    private val dateLabelFormatter = DateTimeFormatter.ofPattern("dd/MMM")

    override fun now(): Long = System.currentTimeMillis()

    override fun getCurrentDayOfWeek(): Int = getDayOfWeekFromMillis(now())

    override fun getDayOfWeekFromMillis(millis: Long): Int = zonedDateTimeFromMillis(millis)
        .toLocalDate()
        .dayOfWeek
        .value

    override fun getMillisForStartOfDayInWeek(dayOfWeek: Int): Long {
        val currentDayOfWeek = getCurrentDayOfWeek()
        val daysFromToday = dayOfWeek - currentDayOfWeek
        return getStartOfDayMillisWithOffsetFromToday(daysFromToday)
    }

    override fun getDateLabelFromMillis(millis: Long): String =
        zonedDateTimeFromMillis(millis).format(dateLabelFormatter)

    override fun getMillisFromFilter(timeFilter: TimeFilter): Long {
        if (timeFilter == TimeFilter.ALL) return 0L

        val startOfToday = zonedDateTimeFromMillis()
            .toLocalDate()
            .atStartOfDay(timeZone.getTimeZone())

        val resultZonedDateTime = when (timeFilter) {
            TimeFilter.ONE_WEEK -> startOfToday.minusWeeks(1)
            TimeFilter.ONE_MONTH -> startOfToday.minusMonths(1)
            TimeFilter.THREE_MONTH -> startOfToday.minusMonths(3)
            TimeFilter.SIX_MONTH -> startOfToday.minusMonths(6)
            TimeFilter.ONE_YEAR -> startOfToday.minusYears(1)
            else -> return 0L
        }

        return resultZonedDateTime.toInstant().toEpochMilli()
    }

    private fun zonedDateTimeFromMillis(millis: Long = now()) = Instant
        .ofEpochMilli(millis)
        .atZone(timeZone.getTimeZone())

    private fun getStartOfDayMillisWithOffsetFromToday(offsetInDays: Int): Long =
        zonedDateTimeFromMillis()
            .toLocalDate()
            .atStartOfDay(timeZone.getTimeZone())
            .plusDays(offsetInDays.toLong())
            .toInstant()
            .toEpochMilli()
}