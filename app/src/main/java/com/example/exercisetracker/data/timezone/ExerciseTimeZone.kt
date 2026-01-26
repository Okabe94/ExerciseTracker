package com.example.exercisetracker.data.timezone

import com.example.exercisetracker.domain.timezone.AppTimeZone
import java.time.ZoneId

class ExerciseTimeZone : AppTimeZone{
    override fun getTimeZone(): ZoneId = ZoneId.systemDefault()
}