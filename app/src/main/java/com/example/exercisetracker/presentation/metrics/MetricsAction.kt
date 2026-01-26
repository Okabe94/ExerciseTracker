package com.example.exercisetracker.presentation.metrics

import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.filter.TypeFilter

sealed interface MetricsAction {
    data class OnMuscleSelected(val muscle: Muscle) : MetricsAction
    data class OnExerciseSelected(val exercise: Exercise) : MetricsAction
    data class OnTimeSelected(val time: TimeFilter) : MetricsAction
    data class OnTypeSelected(val type: TypeFilter) : MetricsAction
    data class OnExpandedChange(val expanded: Boolean) : MetricsAction
}
