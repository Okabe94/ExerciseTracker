package com.example.exercisetracker.presentation.metrics

import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.filter.TypeFilter
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle

sealed interface MetricsAction {
    data class OnMuscleSelected(val muscle: Muscle) : MetricsAction
    data class OnExerciseSelected(val exercise: Exercise) : MetricsAction
    data class OnExpandedChange(val expanded: Boolean) : MetricsAction
    data class OnTimeSelected(val time: TimeFilter) : MetricsAction
    data class OnTypeSelected(val type: TypeFilter) : MetricsAction
    data class OnDeleteSet(val setId: Int) : MetricsAction
    data class OnToggleExpandSet(val label: String) : MetricsAction
    data class OnShowDeleteConfirmation(val setId: Int?) : MetricsAction
}
