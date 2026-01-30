package com.example.exercisetracker.data.local.model

data class WorkoutReview (
    val startTime: Long,
    val setNumber: Int,
    val name: String,
    val weight: Float,
    val reps: Int
)