package com.example.exercisetracker.domain.time

import com.example.exercisetracker.domain.filter.TimeFilter

interface AppClock {
    fun now(): Long
    fun getCurrentDayOfWeek(): Int
    fun getDayOfWeekFromMillis(millis: Long): Int
    fun getMillisForStartOfDayInWeek(dayOfWeek: Int): Long
    fun getDateLabelFromMillis(millis: Long): String
    fun getMillisFromFilter(timeFilter: TimeFilter): Long
}