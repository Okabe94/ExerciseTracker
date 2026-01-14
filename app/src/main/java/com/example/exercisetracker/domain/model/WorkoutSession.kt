package com.example.exercisetracker.domain.model

data class WorkoutSession(
    val id: Int = 0,
    val startTime: Long,
    val endTime: Long?,
    val exercises: List<Int>,
    val isCompleted: Boolean = false
)