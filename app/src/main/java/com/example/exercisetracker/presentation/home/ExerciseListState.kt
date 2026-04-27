package com.example.exercisetracker.presentation.home

import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.model.Routine

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
    val plannedTodayDialogVisible: Boolean = false,
    val plannedExercisesForToday: List<Int> = emptyList(),
    val selectedMuscleIds: List<Int> = emptyList(),
    val selectedExerciseIds: List<Int> = emptyList(),
    val muscleList: List<Muscle> = emptyList(),
    val exerciseList: List<Exercise> = emptyList(),
    val allExercises: List<Exercise> = emptyList(),
    val routines: List<Routine> = emptyList(),
    val routinesSheetVisible: Boolean = false,
    val routineSheetContent: RoutineSheetContent = RoutineSheetContent.List,
    val routineEditorState: RoutineEditorState = RoutineEditorState(),
)

sealed interface ScreenMode {
    data object Workout : ScreenMode
    data object Review : ScreenMode
    data class Planning(val day: Int) : ScreenMode
}

sealed interface RoutineSheetContent {
    data object List : RoutineSheetContent
    data class Editor(val routineId: Int?) : RoutineSheetContent
}

data class RoutineEditorState(
    val name: String = "",
    val selectedExerciseIds: Set<Int> = emptySet(),
    val selectedMuscleIds: Set<Int> = emptySet()
)
