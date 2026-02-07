package com.example.exercisetracker.domain.time

import com.example.exercisetracker.domain.filter.TimeFilter

interface AppClock {
    fun now(): Long
    fun getDayNumberFromMillis(millis: Long): Int
    fun getCurrentNumberDay(): Int
    fun getDateLabelFromMillis(millis: Long): String
    fun getMillisForDay(daysFromToday: Int): Long
    fun getMillisAtStartOfDay(daysFromToday: Int): Long
    fun getMillisFromFilter(timeFilter: TimeFilter): Long
}