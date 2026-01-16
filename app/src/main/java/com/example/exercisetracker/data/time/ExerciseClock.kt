package com.example.exercisetracker.data.time

import com.example.exercisetracker.domain.time.AppClock

class ExerciseClock : AppClock {
    override fun now(): Long = System.currentTimeMillis()
}