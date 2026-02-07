package com.example.exercisetracker.domain.model

data class WorkoutPlan(
    val id: Int = 0,
    val day: Int,
    val exercises: List<Int>
)
