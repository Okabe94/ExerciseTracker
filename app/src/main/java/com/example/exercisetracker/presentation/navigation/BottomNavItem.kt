package com.example.exercisetracker.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.exercisetracker.R

data class BottomNavItem(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
)

/**
 * Top level destinations refers to the bottom navigation items
 * and contain the information needed to display the icon and text
 * as well as functioning as ids for the different backstacks
 */
val TOP_LEVEL_DESTINATION = mapOf(
    Route.Home to BottomNavItem(
        icon = R.drawable.outline_exercise_24,
        title = R.string.training
    ),
    Route.Progress to BottomNavItem(
        icon = R.drawable.outline_bar_chart_24,
        title = R.string.metrics
    )
)
