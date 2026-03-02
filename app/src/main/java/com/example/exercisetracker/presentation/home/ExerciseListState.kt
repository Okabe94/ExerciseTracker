package com.example.exercisetracker.presentation.home

import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle

data class ExerciseListState(
    val screenMode: ScreenMode = ScreenMode.Workout,
    val workoutDaysDone: Set<Int> = emptySet(),
    val plannedWorkouts: Set<Int> = emptySet(),
    val currentDay: Int = 0,
    val daySelected: Int = 1,
    val searchQuery: String = "",
    val hasActiveWorkout: Boolean = false,
    val startWorkoutButtonVisible: Boolean = false,
    val addMuscleDialogVisible: Boolean = false,
    val addExerciseDialogVisible: Boolean = false,
    val activeWorkoutDialogVisible: Boolean = false,
    val deletePlanDialogVisible: Boolean = false,
    val deleteDayWorkoutDialogVisible: Boolean = false,
    val selectedMuscleIds: List<Int> = emptyList(),
    val selectedExerciseIds: List<Int> = emptyList(),
    val muscleList: List<Muscle> = emptyList(),
    val exerciseList: List<Exercise> = emptyList(),
)

sealed interface ScreenMode {
    data object Workout : ScreenMode
    data object Review : ScreenMode
    data class Planning(val day: Int) : ScreenMode
}