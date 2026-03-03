@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.exercisetracker.presentation.metrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.filter.TypeFilter
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.repository.IExerciseRepository
import com.example.exercisetracker.domain.repository.IMuscleRepository
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import com.example.exercisetracker.domain.time.AppClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private const val connerConstant = 0.025

class MetricsViewModel(
    private val clock: AppClock,
    private val workoutRepository: IWorkoutRepository,
    private val exerciseRepository: IExerciseRepository,
    private val muscleRepository: IMuscleRepository
) : ViewModel() {

    private val _musclesAndExercises = combine(
        muscleRepository.allMuscles(),
        exerciseRepository.allExercises()
    ) { muscles, exercises -> Pair(muscles, exercises) }

    private val _filterState = MutableStateFlow(FilterState())
    private val _internalState = MutableStateFlow(InternalState())

    private val _graphDataState = _filterState.flatMapLatest { filters ->
        if (filters.selectedExercise != null) {
            workoutRepository.getGraphData(
                timeFilter = filters.timeSelected,
                exerciseId = filters.selectedExercise.id
            )
        } else {
            flowOf(emptyList())
        }
    }

    val state = combine(
        _musclesAndExercises,
        _filterState,
        _internalState,
        _graphDataState
    ) { musclesAndExercises, filter, internal, graph ->
        val filteredExercises =
            if (internal.muscleSelected != null) {
                musclesAndExercises.second.filter { it.targetMuscleId == internal.muscleSelected.id }
            } else {
                musclesAndExercises.second
            }

        val maxWeight = graph.maxOfOrNull { it.weight } ?: 0f
        val averageReps = try {
            graph.sumOf { it.reps }.div(graph.size)
        } catch (e: Exception) {
            0
        }

        val rm = graph.maxByOrNull { it.weight }?.let {
            it.weight * (1 + connerConstant * it.reps)
        } ?: 0.0

        val graphList = graph.groupBy { clock.getDateLabelFromMillis(it.startTime) }
            .mapValues { entry ->
                entry.value.map {
                    if (filter.typeSelected == TypeFilter.WEIGHT) {
                        it.weight.toDouble()
                    } else {
                        it.reps.toDouble()
                    }
                }
            }

        MetricsState(
            filteredMuscleId = internal.muscleSelected?.id ?: 0,
            selectedExercise = filter.selectedExercise,
            expandedExerciseSelection = internal.expandedList,
            muscleList = musclesAndExercises.first,
            exerciseList = filteredExercises,
            averageReps = averageReps,
            maxWeight = maxWeight,
            rm = rm,
            timeFilterSelected = filter.timeSelected,
            timeFilterOptions = filter.timeFilterOptions,
            typeFilterSelected = filter.typeSelected,
            graphPoints = graphList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MetricsState()
    )

    fun onAction(action: MetricsAction) {
        when (action) {
            is MetricsAction.OnMuscleSelected -> _internalState.update { state ->
                val newMuscle =
                    if (state.muscleSelected == action.muscle) null
                    else action.muscle
                state.copy(muscleSelected = newMuscle)
            }

            is MetricsAction.OnExerciseSelected -> {
                _filterState.update { state -> state.copy(selectedExercise = action.exercise) }
                _internalState.update { state -> state.copy(expandedList = false) }
            }

            is MetricsAction.OnExpandedChange -> _internalState.update { state ->
                state.copy(expandedList = action.expanded)
            }

            is MetricsAction.OnTimeSelected -> _filterState.update { state ->
                state.copy(timeSelected = action.time)
            }

            is MetricsAction.OnTypeSelected -> _filterState.update { state ->
                state.copy(typeSelected = action.type)
            }
        }
    }

    private data class InternalState(
        val expandedList: Boolean = false,
        val muscleSelected: Muscle? = null,
    )

    private data class FilterState(
        val selectedExercise: Exercise? = null,
        val timeSelected: TimeFilter = TimeFilter.ONE_WEEK,
        val typeSelected: TypeFilter = TypeFilter.WEIGHT,
        val timeFilterOptions: List<TimeFilter> = listOf(
            TimeFilter.ONE_WEEK,
            TimeFilter.ONE_MONTH,
            TimeFilter.THREE_MONTH,
            TimeFilter.SIX_MONTH,
            TimeFilter.ONE_YEAR,
            TimeFilter.ALL,
        )
    )
}
