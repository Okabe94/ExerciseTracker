@file:OptIn(ExperimentalTime::class)

package com.example.exercisetracker.data.time

import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.time.AppClock
import com.example.exercisetracker.domain.timezone.AppTimeZone
import java.time.Instant
import kotlin.time.ExperimentalTime

class ExerciseClock(
    private val timeZone: AppTimeZone
) : AppClock {

    override fun now(): Long = System.currentTimeMillis()

    override fun getCurrentDay(): Int = Instant.ofEpochMilli(now())
        .atZone(timeZone.getTimeZone())
        .toLocalDate()
        .dayOfWeek
        .value

    override fun millisThen(timeFilter: TimeFilter): Long {
        val now = Instant
            .ofEpochMilli(now())
            .atZone(timeZone.getTimeZone())
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        return when (timeFilter) {
            TimeFilter.ALL -> 0L
            TimeFilter.ONE_MONTH -> now.minusMonths(1).toInstant().toEpochMilli()
            TimeFilter.THREE_MONTH -> now.minusMonths(3).toInstant().toEpochMilli()
            TimeFilter.SIX_MONTH -> now.minusMonths(6).toInstant().toEpochMilli()
            TimeFilter.ONE_YEAR -> now.minusMonths(12).toInstant().toEpochMilli()
        }
    }
}