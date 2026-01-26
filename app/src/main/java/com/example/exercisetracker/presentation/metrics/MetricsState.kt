package com.example.exercisetracker.presentation.metrics

import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.filter.TypeFilter

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
    val rm: Double = 0.0,
    val typeFilterSelected: TypeFilter = TypeFilter.REPS,
    val timeFilterSelected: TimeFilter = TimeFilter.ALL,
    val timeFilterOptions: List<TimeFilter> = emptyList(),
    val graphPoints: List<GraphPoints> = emptyList()
)

data class GraphPoints(
    val value: Float = 0f,
    val description: String = ""
)


