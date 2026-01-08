package com.example.exercisetracker.domain.model

data class Exercise(
    val id: Int = 0,
    val name: String,
    val targetMuscleId: Int
)