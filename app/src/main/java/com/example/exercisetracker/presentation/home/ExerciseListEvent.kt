package com.example.exercisetracker.presentation.home

import com.example.exercisetracker.core.presentation.util.UiText

sealed interface ExerciseListEvent {
    data object WorkoutSaved : ExerciseListEvent
    data object WorkoutUpdated : ExerciseListEvent
    data object WorkoutDeleted : ExerciseListEvent
    data class SendToReview(val day: Int) : ExerciseListEvent
    data class ErrorMessage(val reason: UiText) : ExerciseListEvent
}