package com.example.exercisetracker.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Home : Route

    @Serializable
    data object Workout : Route

    @Serializable
    data class Review(val day: Int) : Route

    @Serializable
    data object Metrics : Route
}