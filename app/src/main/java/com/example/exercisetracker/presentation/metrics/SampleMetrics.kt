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
    graphPoints = GraphData(
        mapOf(
            "20/Feb" to 5.0,
            "21/Feb" to 5.0,
            "22/Feb" to 1.0,
            "23/Feb" to 5.0,
            "24/Feb" to 2.0,
            "25/Feb" to 1.0,
            "26/Feb" to 1.5
        )
    ),
    totalVolume = 12500.0,
    totalSessions = 8,
    totalSets = 42,
    prWeight = 100f,
    prReps = 5,
    prDate = "15/Mar",
    totalWorkoutsAllTime = 47,
    workoutsThisWeek = 3,
    avgWorkoutsPerWeek = 3.8
)
