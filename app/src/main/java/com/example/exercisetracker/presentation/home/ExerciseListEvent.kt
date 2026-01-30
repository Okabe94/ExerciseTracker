package com.example.exercisetracker.presentation.home

import com.example.exercisetracker.core.presentation.util.UiText

sealed interface ExerciseListEvent {
    data class ErrorMessage(val reason: UiText) : ExerciseListEvent
}