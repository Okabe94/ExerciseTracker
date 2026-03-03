package com.example.exercisetracker.presentation.metrics

import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.filter.TypeFilter
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle

val sampleMetricsState = MetricsState(
    filteredMuscleId = 1,
    selectedExercise = Exercise(
        id = 1,
        name = "Bicep Curls",
        targetMuscleId = 1
    ),
    expandedExerciseSelection = false,
    muscleList = listOf(
        Muscle(id = 1, name = "Biceps"),
        Muscle(id = 2, name = "Triceps"),
        Muscle(id = 3, name = "Shoulders")
    ),
    exerciseList = listOf(
        Exercise(id = 1, name = "Bicep Curls", targetMuscleId = 1),
        Exercise(id = 2, name = "Tricep Dips", targetMuscleId = 2),
        Exercise(id = 3, name = "Shoulder Press", targetMuscleId = 3)
    ),
    averageReps = 1000,
    maxWeight = 100f,
    rm = 120.0,
    typeFilterSelected = TypeFilter.WEIGHT,
    timeFilterSelected = TimeFilter.ALL,
    timeFilterOptions = listOf(
        TimeFilter.ALL,
        TimeFilter.ONE_MONTH,
        TimeFilter.THREE_MONTH,
        TimeFilter.SIX_MONTH,
        TimeFilter.ONE_YEAR
    ),
    graphPoints = mapOf(
        "20/Feb" to listOf(5.0, 4.0, 7.0, 1.1),
        "21/Feb" to listOf(5.0, 4.0, 1.1),
        "22/Feb" to listOf(1.0, 5.0, 4.0, 7.0),
        "23/Feb" to listOf(5.0, 4.0, 7.0),
        "24/Feb" to listOf(2.0, 5.0, 4.0, 7.0),
        "25/Feb" to listOf(1.0, 2.0, 5.0, 4.0, 7.0),
        "26/Feb" to listOf(1.5, 2.2, 4.0, 7.0),
    )
)
