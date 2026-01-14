package com.example.exercisetracker.presentation.workout

data class WorkoutSessionState(
    val sessionId: Int = 0,
    val listOfExercises: List<WorkoutSessionExercise> = emptyList()
)

data class WorkoutSessionExercise(
    val name: String = "",
    val id: Int = 0,
    val sets: List<WorkoutSessionSet> = emptyList()
)

data class WorkoutSessionSet(
    val id: Int = 0,
    val number: Int = 0,
    val weight: String = "",
    val reps: String = ""
)