package com.example.exercisetracker.domain.timezone

import java.time.ZoneId

interface AppTimeZone {
    fun getTimeZone(): ZoneId
}