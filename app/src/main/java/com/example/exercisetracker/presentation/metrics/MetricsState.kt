package com.example.exercisetracker.presentation.metrics

import com.example.exercisetracker.data.local.model.MetricGraphData
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
    val graphPoints: Map<String, Double> = emptyMap(),
    val groupedSets: Map<String, List<MetricGraphData>> = emptyMap(),
    val expandedSets: Set<String> = emptySet(),
    val showDeleteConfirmation: Boolean = false,
    val setIdToDelete: Int? = null,
    val totalVolume: Double = 0.0,
    val totalSessions: Int = 0,
    val totalSets: Int = 0,
    val prWeight: Float = 0f,
    val prReps: Int = 0,
    val prDate: String = "",
    val totalWorkoutsAllTime: Int = 0,
    val workoutsThisWeek: Int = 0,
    val avgWorkoutsPerWeek: Double = 0.0
)
