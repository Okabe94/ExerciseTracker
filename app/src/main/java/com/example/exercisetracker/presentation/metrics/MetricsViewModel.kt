@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.exercisetracker.presentation.metrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercisetracker.data.local.model.MetricGraphData
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
import kotlinx.coroutines.launch

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

    private val _bestSetState = _filterState.flatMapLatest { filters ->
        if (filters.selectedExercise != null)
            workoutRepository.getBestSet(filters.selectedExercise.id)
        else flowOf(null)
    }

    init {
        viewModelScope.launch {
            workoutRepository.getTotalWorkoutCount().collect { count ->
                _internalState.update { it.copy(totalWorkoutsAllTime = count) }
            }
        }
        viewModelScope.launch {
            workoutRepository.getWorkoutsThisWeek(
                clock.getMillisForStartOfDayInWeek(1)
            ).collect { count ->
                _internalState.update { it.copy(workoutsThisWeek = count) }
            }
        }
        viewModelScope.launch {
            workoutRepository.getFirstWorkoutTime().collect { time ->
                _internalState.update { it.copy(firstWorkoutTime = time) }
            }
        }
    }

    val state = combine(
        _musclesAndExercises,
        _filterState,
        _internalState,
        _graphDataState,
        _bestSetState
    ) { musclesAndExercises, filter, internal, graph, bestSet ->
        val filteredExercises =
            if (internal.muscleSelected != null) {
                musclesAndExercises.second.filter { it.targetMuscleId == internal.muscleSelected.id }
            } else {
                musclesAndExercises.second
            }

        val maxWeight = graph.maxOfOrNull { it.weight } ?: 0f
        val averageReps = if (graph.isEmpty()) 0 else {
            try {
                graph.sumOf { it.reps }.div(graph.size)
            } catch (e: Exception) {
                0
            }
        }

        val rm = graph.maxByOrNull { it.weight }?.let {
            it.weight * (1 + connerConstant * it.reps)
        } ?: 0.0

        val totalVolume = graph.sumOf { it.weight.toDouble() * it.reps }
        val totalSets = graph.size
        val totalSessions = graph.map { it.sessionId }.distinct().size
        val avgPerWeek = if (internal.firstWorkoutTime == null || internal.totalWorkoutsAllTime == 0) 0.0
        else {
            val weeks = (clock.now() - internal.firstWorkoutTime) / (7.0 * 24 * 3600 * 1000)
            internal.totalWorkoutsAllTime / weeks.coerceAtLeast(1.0)
        }
        val prDate = bestSet?.let { clock.getDateLabelFromMillis(it.startTime) } ?: ""

        val processedData = processGraphData(
            data = graph,
            timeFilter = filter.timeSelected,
            typeFilter = filter.typeSelected
        )

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
            graphPoints = processedData.first,
            groupedSets = processedData.second,
            expandedSets = internal.expandedSets,
            showDeleteConfirmation = internal.setIdToDelete != null,
            setIdToDelete = internal.setIdToDelete,
            totalVolume = totalVolume,
            totalSessions = totalSessions,
            totalSets = totalSets,
            prWeight = bestSet?.weight ?: 0f,
            prReps = bestSet?.reps ?: 0,
            prDate = prDate,
            totalWorkoutsAllTime = internal.totalWorkoutsAllTime,
            workoutsThisWeek = internal.workoutsThisWeek,
            avgWorkoutsPerWeek = avgPerWeek
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MetricsState()
    )

    private fun processGraphData(
        data: List<MetricGraphData>,
        timeFilter: TimeFilter,
        typeFilter: TypeFilter
    ): Pair<Map<String, Double>, Map<String, List<MetricGraphData>>> {
        if (data.isEmpty()) return Pair(emptyMap(), emptyMap())

        val grouped = when (timeFilter) {
            TimeFilter.ONE_WEEK -> data.groupBy { it.startTime / (24 * 3600000L) }
            TimeFilter.ONE_MONTH -> data.groupBy { it.startTime / (7 * 24 * 3600000L) }
            TimeFilter.THREE_MONTH,
            TimeFilter.SIX_MONTH -> data.groupBy { it.startTime / (14 * 24 * 3600000L) }

            TimeFilter.ONE_YEAR -> data.groupBy { it.startTime / (30 * 24 * 3600000L) }
            TimeFilter.ALL -> {
                val sorted = data.sortedBy { it.startTime }
                val start = sorted.first().startTime
                val end = sorted.last().startTime
                val range = (end - start).coerceAtLeast(1L)
                val bucketSize = (range / 12).coerceAtLeast(1L)
                data.groupBy { ((it.startTime - start) / bucketSize).coerceAtMost(11L) }
            }
        }

        val firstBucketKey = grouped.keys.minOrNull() ?: 0L
        val graphPoints = mutableMapOf<String, Double>()
        val groupedSets = mutableMapOf<String, List<MetricGraphData>>()

        grouped.toSortedMap().forEach { (key, bucketData) ->
            val firstInBucket = bucketData.minBy { it.startTime }
            val relativeIndex = (key - firstBucketKey).toInt() + 1

            val label = when (timeFilter) {
                TimeFilter.ONE_WEEK -> clock.getDateLabelFromMillis(firstInBucket.startTime)
                TimeFilter.ONE_MONTH -> "Semana $relativeIndex"
                TimeFilter.THREE_MONTH, TimeFilter.SIX_MONTH -> "Par semana $relativeIndex"
                TimeFilter.ONE_YEAR -> "Mes $relativeIndex"
                TimeFilter.ALL -> "Periodo $relativeIndex"
            }

            val value = when (typeFilter) {
                TypeFilter.WEIGHT -> bucketData.sumOf { it.weight.toDouble() } / bucketData.size
                TypeFilter.REPS -> bucketData.sumOf { it.reps }.toDouble()
                TypeFilter.VOLUME -> bucketData.sumOf { it.weight.toDouble() * it.reps }
            }

            graphPoints[label] = value
            groupedSets[label] = bucketData
        }

        return Pair(graphPoints, groupedSets)
    }

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

            is MetricsAction.OnDeleteSet -> {
                viewModelScope.launch {
                    workoutRepository.deleteSet(action.setId)
                    _internalState.update { it.copy(setIdToDelete = null) }
                }
            }

            is MetricsAction.OnToggleExpandSet -> _internalState.update { state ->
                val newExpanded = if (state.expandedSets.contains(action.label)) {
                    state.expandedSets - action.label
                } else {
                    state.expandedSets + action.label
                }
                state.copy(expandedSets = newExpanded)
            }

            is MetricsAction.OnShowDeleteConfirmation -> _internalState.update { it.copy(setIdToDelete = action.setId) }
        }
    }

    private data class InternalState(
        val expandedList: Boolean = false,
        val muscleSelected: Muscle? = null,
        val expandedSets: Set<String> = emptySet(),
        val setIdToDelete: Int? = null,
        val totalWorkoutsAllTime: Int = 0,
        val workoutsThisWeek: Int = 0,
        val firstWorkoutTime: Long? = null
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
