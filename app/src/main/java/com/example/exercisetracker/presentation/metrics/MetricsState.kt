package com.example.exercisetracker.presentation.metrics

import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.filter.TypeFilter

data class MetricsState(
    val filteredMuscleId: Int = 0,
    val selectedExercise: Exercise? = null,
    val expandedExerciseSelection: Boolean = false,
    val muscleList: List<Muscle> = emptyList(),
    val exerciseList: List<Exercise> = emptyList(),
    val averageReps: Int = 0,
    val maxWeight: Float = 0f,
    val rm: Double = 0.0,
    val typeFilterSelected: TypeFilter = TypeFilter.REPS,
    val timeFilterSelected: TimeFilter = TimeFilter.ALL,
    val timeFilterOptions: List<TimeFilter> = emptyList(),
    val graphPoints: Map<String, List<Double>> = emptyMap()
)


