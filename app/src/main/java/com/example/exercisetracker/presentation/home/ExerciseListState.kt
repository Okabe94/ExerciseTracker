package com.example.exercisetracker.presentation.home

import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle

data class ExerciseListState(
    val searchQuery: String = "",
    val hasActiveWorkout: Boolean = false,
    val startWorkoutButtonVisible: Boolean = false,
    val addMuscleDialogVisible: Boolean = false,
    val addExerciseDialogVisible: Boolean = false,
    val confirmDialogVisible: Boolean = false,
    val selectedMuscleIds: List<Int> = emptyList(),
    val selectedExerciseIds: List<Int> = emptyList(),
    val muscleList: List<Muscle> = emptyList(),
    val exerciseList: List<Exercise> = emptyList(),
)