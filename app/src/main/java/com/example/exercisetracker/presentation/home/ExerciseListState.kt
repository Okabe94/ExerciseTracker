package com.example.exercisetracker.presentation.home

import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle

data class ExerciseListState(
    val searchQuery: String = "",
    val activeWorkoutButtonVisible: Boolean = false,
    val startWorkoutButtonVisible: Boolean = false,
    val addMuscleDialogVisible: Boolean = false,
    val addExerciseDialogVisible: Boolean = false,
    val selectedMuscleIds: Set<Int> = emptySet(),
    val selectedExerciseIds: Set<Int> = emptySet(),
    val filteredMuscles: List<Muscle> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
)