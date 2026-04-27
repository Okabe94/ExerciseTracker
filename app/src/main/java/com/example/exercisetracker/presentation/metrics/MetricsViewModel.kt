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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
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

    private data class ProcessedExerciseData(
        val rawData: List<MetricGraphData>,
        val graphPoints: GraphData,
        val groupedSets: Map<String, List<MetricGraphData>>
    )

    private val _processedGraphData = combine(
        _filterState,
        _graphDataState
    ) { filter, graph ->
        val (points, grouped) = processGraphData(graph, filter.timeSelected, filter.typeSelected)
        ProcessedExerciseData(
            rawData = graph,
            graphPoints = GraphData(points),
            groupedSets = grouped
        )
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
        _processedGraphData,
        _bestSetState
    ) { musclesAndExercises, filter, internal, processedData, bestSet ->
        val filteredExercises =
            if (internal.muscleSelected != null) {
                musclesAndExercises.second.filter { it.targetMuscleId == internal.muscleSelected.id }
            } else {
                musclesAndExercises.second
            }

        val graph = processedData.rawData
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
            graphPoints = processedData.graphPoints,
            groupedSets = processedData.groupedSets,
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

    private fun spanishDayAbbr(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "Lun"
        DayOfWeek.TUESDAY -> "Mar"
        DayOfWeek.WEDNESDAY -> "Mié"
        DayOfWeek.THURSDAY -> "Jue"
        DayOfWeek.FRIDAY -> "Vie"
        DayOfWeek.SATURDAY -> "Sáb"
        DayOfWeek.SUNDAY -> "Dom"
    }

    private fun spanishMonthAbbr(month: Month): String = when (month) {
        Month.JANUARY -> "Ene"
        Month.FEBRUARY -> "Feb"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Abr"
        Month.MAY -> "May"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Ago"
        Month.SEPTEMBER -> "Sep"
        Month.OCTOBER -> "Oct"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dic"
    }

    private fun processGraphData(
        data: List<MetricGraphData>,
        timeFilter: TimeFilter,
        typeFilter: TypeFilter
    ): Pair<Map<String, Double>, Map<String, List<MetricGraphData>>> {
        if (data.isEmpty()) return Pair(emptyMap(), emptyMap())

        val sortedData = data.sortedBy { it.startTime }

        val allMonthsBetween = if (timeFilter == TimeFilter.ALL) {
            val startDate = clock.toLocalDate(sortedData.first().startTime)
            val endDate = clock.toLocalDate(sortedData.last().startTime)
            ChronoUnit.MONTHS.between(startDate, endDate)
        } else 0L

        // Maps sortKey -> (displayLabel, entries)
        val bucketMap = mutableMapOf<String, Pair<String, MutableList<MetricGraphData>>>()
        val epochMonday = LocalDate.of(2000, 1, 3) // reference Monday for week-pair grouping

        for (entry in sortedData) {
            val date = clock.toLocalDate(entry.startTime)
            val yr = date.year.toString().takeLast(2)

            val (sortKey, label) = when (timeFilter) {
                TimeFilter.ONE_WEEK -> {
                    date.toString() to "${spanishDayAbbr(date.dayOfWeek)} ${date.dayOfMonth}"
                }
                TimeFilter.ONE_MONTH -> {
                    val wf = WeekFields.ISO
                    val weekYear = date.get(wf.weekBasedYear())
                    val weekNum = date.get(wf.weekOfWeekBasedYear())
                    val monday = date.with(DayOfWeek.MONDAY)
                    "%04d-%02d".format(weekYear, weekNum) to
                        "%02d/%02d".format(monday.dayOfMonth, monday.monthValue)
                }
                TimeFilter.THREE_MONTH -> {
                    val monday = date.with(DayOfWeek.MONDAY)
                    val absoluteWeek = ChronoUnit.WEEKS.between(epochMonday, monday)
                    val pairIndex = absoluteWeek / 2
                    val periodStart = epochMonday.plusWeeks(pairIndex * 2)
                    "%010d".format(pairIndex) to
                        "%02d/%02d".format(periodStart.dayOfMonth, periodStart.monthValue)
                }
                TimeFilter.SIX_MONTH -> {
                    "%04d-%02d".format(date.year, date.monthValue) to
                        spanishMonthAbbr(date.month)
                }
                TimeFilter.ONE_YEAR -> {
                    "%04d-%02d".format(date.year, date.monthValue) to
                        "${spanishMonthAbbr(date.month)} '$yr"
                }
                TimeFilter.ALL -> when {
                    allMonthsBetween <= 12L -> {
                        "%04d-%02d".format(date.year, date.monthValue) to
                            "${spanishMonthAbbr(date.month)} '$yr"
                    }
                    allMonthsBetween <= 24L -> {
                        val biMonth = (date.monthValue - 1) / 2
                        val startMonth = Month.of(biMonth * 2 + 1)
                        "%04d-%d".format(date.year, biMonth) to
                            "${spanishMonthAbbr(startMonth)} '$yr"
                    }
                    else -> {
                        val quarter = (date.monthValue - 1) / 3 + 1
                        "%04d-%d".format(date.year, quarter) to "T$quarter '$yr"
                    }
                }
            }

            bucketMap.getOrPut(sortKey) { label to mutableListOf() }.second.add(entry)
        }

        val graphPoints = mutableMapOf<String, Double>()
        val groupedSets = mutableMapOf<String, List<MetricGraphData>>()

        bucketMap.toSortedMap().forEach { (_, pair) ->
            val (label, bucketData) = pair
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
