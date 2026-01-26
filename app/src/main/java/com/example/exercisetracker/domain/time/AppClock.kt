package com.example.exercisetracker.domain.time

import com.example.exercisetracker.domain.filter.TimeFilter

interface AppClock {
    fun now(): Long
    fun getCurrentDay(): Int
    fun getDateLabel(millis: Long) : String
    fun millisThen(timeFilter: TimeFilter): Long
}