package com.example.exercisetracker.presentation.metrics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.filter.TypeFilter
import com.example.exercisetracker.domain.repository.IExerciseRepository
import com.example.exercisetracker.domain.repository.IMuscleRepository
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import com.example.exercisetracker.domain.time.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MetricsViewModel(
    private val clock: AppClock,
    private val workoutRepository: IWorkoutRepository,
    private val exerciseRepository: IExerciseRepository,
    private val muscleRepository: IMuscleRepository
) : ViewModel() {

    private val _allMuscles = muscleRepository.allMuscles()
    private val _allExercises = exerciseRepository.allExercises()
    private val _workoutWeek = workoutRepository.getWorkoutDays()
    private val _filterState = MutableStateFlow(FilterState())
    private val _internalState = MutableStateFlow(InternalState())

    // Flow for graph

    init {
        viewModelScope.launch {
            workoutRepository.getGraphData(TimeFilter.ONE_MONTH, 1).collectLatest {
                Log.e("CHAAAO", it.toString())
            }
        }
    }

    val state = combine(
        _workoutWeek,
        _allMuscles,
        _allExercises,
        _filterState,
        _internalState
    ) { workoutWeek, muscles, exercises, filter, internal ->
        val filteredExercises =
            if (internal.muscleSelected != null) {
                exercises.filter { it.targetMuscleId == internal.muscleSelected.id }
            } else {
                exercises
            }

        MetricsState(
            workoutDaysDone = workoutWeek,
            currentDay = clock.getCurrentDay(),
            filteredMuscleId = internal.muscleSelected?.id ?: 0,
            selectedExercise = filter.selectedExercise,
            expandedExerciseSelection = internal.expandedList,
            muscleList = muscles,
            exerciseList = filteredExercises,
            totalVolume = 0f, // TODO
            maxWeight = 0f, // TODO
            rm = 0f, // TODO
            timeFilterSelected = filter.timeSelected,
            timeFilterOptions = filter.timeFilterOptions,
            typeFilterSelected = filter.typeSelected,
            graphPoints = emptyList() // TODO
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
        val timeSelected: TimeFilter = TimeFilter.ONE_MONTH,
        val typeSelected: TypeFilter = TypeFilter.WEIGHT,
        val timeFilterOptions: List<TimeFilter> = listOf(
            TimeFilter.ALL,
            TimeFilter.ONE_MONTH,
            TimeFilter.THREE_MONTH,
            TimeFilter.SIX_MONTH,
            TimeFilter.ONE_YEAR
        )
    )
}
