package com.example.exercisetracker.presentation.home

sealed interface ExerciseListAction {
    data object OnResumeWorkout : ExerciseListAction
    data object OnStartWorkout : ExerciseListAction
    data class OnShowConfirmDialog(val show: Boolean) : ExerciseListAction
    data class OnShowMuscleDialog(val show: Boolean) : ExerciseListAction
    data class OnShowExerciseDialog(val show: Boolean) : ExerciseListAction
    data class OnMuscleSelected(val muscleId: Int) : ExerciseListAction
    data class OnExerciseSelected(val exerciseId: Int) : ExerciseListAction
    data class OnAddExercise(val name: String) : ExerciseListAction
    data class OnAddMuscle(val name: String) : ExerciseListAction
}