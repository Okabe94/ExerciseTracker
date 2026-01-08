package com.example.exercisetracker.domain.model

data class WorkoutSet(
    val id: Int = 0,
    val sessionId: Int,
    val exerciseId: Int,
    val setNumber: Int,
    val weight: Float,
    val reps: Int
)
