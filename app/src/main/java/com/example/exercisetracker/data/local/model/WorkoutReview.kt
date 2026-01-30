package com.example.exercisetracker.data.local.model

data class WorkoutReview(
    val sessionId: Int,
    val setId: Int,
    val startTime: Long,
    val setNumber: Int,
    val muscleName: String,
    val exerciseName: String,
    val weight: Float,
    val reps: Int
)