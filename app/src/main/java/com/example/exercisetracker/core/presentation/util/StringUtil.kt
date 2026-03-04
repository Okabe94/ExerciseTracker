package com.example.exercisetracker.core.presentation.util

import java.util.Locale.getDefault

fun String.cap(): String = this.replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(
        getDefault()
    ) else it.toString()
}
