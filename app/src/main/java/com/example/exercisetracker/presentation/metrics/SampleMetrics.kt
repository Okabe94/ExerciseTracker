package com.example.exercisetracker.presentation.metrics

import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.filter.TypeFilter
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle

val sampleMetricsState = MetricsState(
    workoutDaysDone = setOf(1, 2, 3),
    currentDay = 3,
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
    totalVolume = 1000f,
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
    graphPoints = listOf(
        GraphPoints(value = 10f, description = "Jan"),
        GraphPoints(value = 20f, description = "Feb"),
        GraphPoints(value = 30f, description = "Mar"),
        GraphPoints(value = 40f, description = "Apr"),
        GraphPoints(value = 50f, description = "May")
    )
)
