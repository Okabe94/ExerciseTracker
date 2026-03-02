package com.example.exercisetracker.presentation.home

sealed interface ExerciseListAction {
    data object OnResumeWorkout : ExerciseListAction
    data object OnStartWorkout : ExerciseListAction
    data object OnReturnToWorkout : ExerciseListAction
    data object OnDeletePlannedWorkout : ExerciseListAction
    data object OnDeleteDayWorkout: ExerciseListAction

    data object OnRedoWorkout : ExerciseListAction
    data class OnSaveWorkout(val isUpdate: Boolean) : ExerciseListAction
    data class OnNavigateToReview(val day: Int) : ExerciseListAction
    data class OnDayNodeSelected(val day: Int) : ExerciseListAction
    data class OnMuscleSelected(val muscleId: Int) : ExerciseListAction
    data class OnExerciseSelected(val exerciseId: Int) : ExerciseListAction
    data class OnAddExercise(val name: String) : ExerciseListAction
    data class OnAddMuscle(val name: String) : ExerciseListAction

    data class OnShowDeletePlannedDialog(val show: Boolean) : ExerciseListAction
    data class OnShowDeleteWorkoutDialog(val show: Boolean) : ExerciseListAction
    data class OnShowActiveWorkoutDialog(val show: Boolean) : ExerciseListAction
    data class OnShowMuscleDialog(val show: Boolean) : ExerciseListAction
    data class OnShowExerciseDialog(val show: Boolean) : ExerciseListAction
}