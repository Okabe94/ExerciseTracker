package com.example.exercisetracker.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object Home : Route, NavKey

    @Serializable
    data object Workout : Route, NavKey
}