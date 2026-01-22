package com.example.exercisetracker.presentation.metrics

import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle

data class MetricsState(
    val workoutDaysDone: Set<Int> = emptySet(),
    val currentDay: Int = 0,
    val filteredMuscleId: Int = 0,
    val selectedExercise: Exercise? = null,
    val expandedExerciseSelection: Boolean = false,
    val muscleList: List<Muscle> = emptyList(),
    val exerciseList: List<Exercise> = emptyList(),
    val totalVolume: Float = 0f,
    val maxWeight: Float = 0f,
    val rm: Float = 0f,
    val typeFilterSelected: TypeFilter = TypeFilter.REPS,
    val timeFilterSelected: TimeFilter = TimeFilter.ALL,
    val timeFilterOptions: List<TimeFilter> = emptyList(),
    val graphPoints: List<GraphPoints> = emptyList()
)

enum class TimeFilter {
    ALL, ONE_MONTH, THREE_MONTH, SIX_MONTH, ONE_YEAR
}

enum class TypeFilter {
    REPS, WEIGHT
}

data class GraphPoints(
    val value: Float = 0f,
    val description: String = ""
)


