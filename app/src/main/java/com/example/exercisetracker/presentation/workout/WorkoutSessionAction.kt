package com.example.exercisetracker.presentation.workout

sealed interface WorkoutSessionAction {
    data object OnFinishWorkout : WorkoutSessionAction
    data class OnAddSet(val exerciseId: Int) : WorkoutSessionAction
    data class OnRemoveSet(val exerciseId: Int, val setId: Int) : WorkoutSessionAction
    data class OnUpdateSet(
        val exerciseId: Int,
        val setId: Int,
        val weight: String,
        val reps: String
    ) : WorkoutSessionAction
}