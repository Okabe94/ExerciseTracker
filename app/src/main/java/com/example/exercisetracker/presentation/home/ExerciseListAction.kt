package com.example.exercisetracker.presentation.home

sealed interface ExerciseListAction {
    data object OnResumeWorkout : ExerciseListAction
    data object OnSaveWorkout : ExerciseListAction
    data object OnStartWorkout : ExerciseListAction
    data object OnReturnToWorkout : ExerciseListAction
    data class OnNavigateToReview(val day: Int) : ExerciseListAction
    data class OnDayNodeSelected(val day: Int) : ExerciseListAction
    data class OnShowConfirmDialog(val show: Boolean) : ExerciseListAction
    data class OnShowMuscleDialog(val show: Boolean) : ExerciseListAction
    data class OnShowExerciseDialog(val show: Boolean) : ExerciseListAction
    data class OnMuscleSelected(val muscleId: Int) : ExerciseListAction
    data class OnExerciseSelected(val exerciseId: Int) : ExerciseListAction
    data class OnAddExercise(val name: String) : ExerciseListAction
    data class OnAddMuscle(val name: String) : ExerciseListAction
}