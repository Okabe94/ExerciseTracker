package com.example.exercisetracker.presentation.home

import com.example.exercisetracker.core.presentation.util.UiText

sealed interface ExerciseListEvent {
    data object WorkoutSaved : ExerciseListEvent
    data class SendToReview(val day: Int) : ExerciseListEvent
    data class ErrorMessage(val reason: UiText) : ExerciseListEvent
}