package com.example.exercisetracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercisetracker.R
import com.example.exercisetracker.core.presentation.util.UiText
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.model.WorkoutPlan
import com.example.exercisetracker.domain.repository.IExerciseRepository
import com.example.exercisetracker.domain.repository.IMuscleRepository
import com.example.exercisetracker.domain.repository.IWorkoutPlanRepository
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import com.example.exercisetracker.domain.time.AppClock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExerciseListViewModel(
    private val clock: AppClock,
    private val muscleRepository: IMuscleRepository,
    private val exerciseRepository: IExerciseRepository,
    private val workoutRepository: IWorkoutRepository,
    private val planRepository: IWorkoutPlanRepository
) : ViewModel() {

    private val _workoutWeek = workoutRepository.getWorkoutDays()
    private val _internalState = MutableStateFlow(InternalState())
    private val _activeWorkout = workoutRepository.getLastActiveSessionFlow()
    private val _plannedWorkout = planRepository.getPlannedWorkouts()

    private val _musclesAndExercises = combine(
        muscleRepository.allMuscles(),
        exerciseRepository.allExercises()
    ) { muscles, exercises ->
        Pair(muscles, exercises)
    }

    private val _eventsChannel = Channel<ExerciseListEvent>()
    val events = _eventsChannel.receiveAsFlow()

    init {
        checkTodayPlannedWorkout()
    }

    private fun checkTodayPlannedWorkout() {
        viewModelScope.launch {
            val today = clock.getCurrentDayOfWeek()
            val planned = _plannedWorkout.first().firstOrNull { it.day == today }
            if (planned != null && planned.exercises.isNotEmpty()) {
                _internalState.update {
                    it.copy(
                        plannedTodayDialogVisible = true,
                        plannedExercisesForToday = planned.exercises
                    )
                }
            }
        }
    }

    val state: StateFlow<ExerciseListState> = combine(
        _workoutWeek,
        _internalState,
        _activeWorkout,
        _musclesAndExercises,
        _plannedWorkout
    ) { daysDone, internal, activeWorkout, musclesAndExercises, plannedWorkout ->

        val filteredExercises = if (internal.selectedMuscleIds.isEmpty()) {
            musclesAndExercises.second
        } else {
            musclesAndExercises.second.filter { it.targetMuscleId in internal.selectedMuscleIds }
        }

        val plannedWeek = plannedWorkout.map { it.day }.toSet()

        val daySelected = if (internal.daySelected == -1) {
            clock.getCurrentDayOfWeek()
        } else {
            internal.daySelected
        }

        ExerciseListState(
            screenMode = internal.screenMode,
            workoutDaysDone = daysDone,
            plannedWorkouts = plannedWeek,
            currentDay = clock.getCurrentDayOfWeek(),
            daySelected = daySelected,
            muscleList = musclesAndExercises.first,
            exerciseList = filteredExercises,
            hasActiveWorkout = activeWorkout != null,
            activeWorkoutDialogVisible = internal.activeWorkoutDialogVisible,
            deletePlanDialogVisible = internal.deletePlanDialogVisible,
            deleteDayWorkoutDialogVisible = internal.deleteDayWorkoutDialogVisible,
            plannedTodayDialogVisible = internal.plannedTodayDialogVisible,
            plannedExercisesForToday = internal.plannedExercisesForToday,
            startWorkoutButtonVisible = internal.selectedExerciseIds.isNotEmpty(),
            selectedMuscleIds = internal.selectedMuscleIds.toList(),
            selectedExerciseIds = internal.selectedExerciseIds.toList(),
            addMuscleDialogVisible = internal.muscleDialogVisible,
            addExerciseDialogVisible = internal.exerciseDialogVisible,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ExerciseListState()
    )

    fun onAction(action: ExerciseListAction) {
        when (action) {
            ExerciseListAction.OnStartWorkout -> startWorkout()
            ExerciseListAction.OnDeletePlannedWorkout -> deletePlannedWorkout()
            ExerciseListAction.OnDeleteDayWorkout -> deleteWorkoutSession()
            ExerciseListAction.OnRedoWorkout -> redoWorkout()
            is ExerciseListAction.OnSaveWorkout -> saveWorkout(action.isUpdate)
            is ExerciseListAction.OnAddMuscle -> addMuscle(action.name)
            is ExerciseListAction.OnAddExercise -> addExercise(action.name)
            is ExerciseListAction.OnDayNodeSelected -> dayNodeAction(action.day)
            is ExerciseListAction.OnShowActiveWorkoutDialog -> toggleActiveWorkoutDialog(action.show)
            is ExerciseListAction.OnShowDeletePlannedDialog -> toggleDeletePlanDialog(action.show)
            is ExerciseListAction.OnShowDeleteWorkoutDialog -> toggleDeleteReviewDialog(action.show)
            is ExerciseListAction.OnMuscleSelected -> toggleMuscleSelection(action.muscleId)
            is ExerciseListAction.OnExerciseSelected -> toggleExerciseSelection(action.exerciseId)

            ExerciseListAction.OnReturnToWorkout -> _internalState.update { state ->
                state.copy(
                    screenMode = ScreenMode.Workout,
                    selectedExerciseIds = mutableSetOf(),
                    selectedMuscleIds = mutableSetOf(),
                    daySelected = clock.getCurrentDayOfWeek()
                )
            }

            is ExerciseListAction.OnShowExerciseDialog -> _internalState.update {
                it.copy(exerciseDialogVisible = action.show)
            }

            is ExerciseListAction.OnShowMuscleDialog -> _internalState.update {
                it.copy(muscleDialogVisible = action.show)
            }

            is ExerciseListAction.OnShowPlannedTodayDialog -> _internalState.update {
                it.copy(plannedTodayDialogVisible = action.show)
            }

            ExerciseListAction.OnStartPlannedWorkout -> {
                val exercises = _internalState.value.plannedExercisesForToday
                _internalState.update {
                    it.copy(
                        selectedExerciseIds = exercises.toMutableSet(),
                        plannedTodayDialogVisible = false
                    )
                }
                startWorkout()
            }

            else -> Unit
        }
    }

    private fun redoWorkout() {
        _internalState.update { state ->
            state.copy(
                screenMode = ScreenMode.Workout,
                selectedMuscleIds = mutableSetOf(),
                daySelected = clock.getCurrentDayOfWeek()
            )
        }
    }

    private fun deleteWorkoutSession() {
        val day = state.value.daySelected
        viewModelScope.launch {
            workoutRepository.deleteWorkoutSession(day)
            _internalState.update { state ->
                state.copy(
                    screenMode = ScreenMode.Workout,
                    daySelected = clock.getCurrentDayOfWeek(),
                    selectedExerciseIds = mutableSetOf(),
                    selectedMuscleIds = mutableSetOf(),
                    deleteDayWorkoutDialogVisible = false
                )
            }
            _eventsChannel.trySend(
                ExerciseListEvent.Message(
                    reason = UiText.StringResource(id = R.string.deleted_workout_message)
                )
            )
        }
    }

    private fun deletePlannedWorkout() {
        viewModelScope.launch {
            val day = _internalState.value.daySelected
            planRepository.deleteWorkoutPlanFromDay(day)
            _eventsChannel.trySend(
                ExerciseListEvent.Message(
                    reason = UiText.StringResource(id = R.string.deleted_workout_message)
                )
            )
            _internalState.update { state ->
                state.copy(
                    screenMode = ScreenMode.Workout,
                    selectedExerciseIds = mutableSetOf(),
                    selectedMuscleIds = mutableSetOf(),
                    deletePlanDialogVisible = false,
                    daySelected = clock.getCurrentDayOfWeek()
                )
            }
        }
    }

    private fun saveWorkout(update: Boolean) {
        val mode = _internalState.value.screenMode
        if (mode !is ScreenMode.Planning) return

        viewModelScope.launch {
            val message: Int
            if (update) {
                message = R.string.updated_workout_message
                planRepository.updateWorkoutPlan(
                    day = mode.day,
                    newExercises = _internalState.value.selectedExerciseIds.toList()
                )
            } else {
                message = R.string.saved_workout_message
                planRepository.insertWorkoutPlan(
                    WorkoutPlan(
                        day = mode.day,
                        exercises = _internalState.value.selectedExerciseIds.toList()
                    )
                )
            }
            _eventsChannel.trySend(
                ExerciseListEvent.Message(
                    reason = UiText.StringResource(id = message)
                )
            )
        }

        _internalState.update { state ->
            state.copy(
                screenMode = ScreenMode.Workout,
                daySelected = clock.getCurrentDayOfWeek(),
                selectedExerciseIds = mutableSetOf(),
                selectedMuscleIds = mutableSetOf()
            )
        }

    }

    private fun startWorkout() {
        viewModelScope.launch {
            toggleActiveWorkoutDialog(false)
            workoutRepository.completeOpenSessions()
            workoutRepository.startNewSession(
                exercises = _internalState.value.selectedExerciseIds.toList()
            )
        }
    }

    private fun dayNodeAction(day: Int) {
        val currentDay = clock.getCurrentDayOfWeek()

        when {
            day < currentDay -> handlePreviousDay(day)

            currentDay < day -> {
                viewModelScope.launch {
                    val planned = _plannedWorkout.first()
                        .firstOrNull { it.day == day }
                        ?.exercises
                        .orEmpty()
                        .toMutableSet()

                    _internalState.update { state ->
                        state.copy(
                            screenMode = ScreenMode.Planning(day),
                            selectedExerciseIds = planned,
                            daySelected = day
                        )
                    }
                }
            }

            else -> {
                if (_internalState.value.screenMode != ScreenMode.Workout) {
                    onAction(ExerciseListAction.OnReturnToWorkout)
                }
            }
        }
    }

    private fun handlePreviousDay(day: Int) {
        viewModelScope.launch {
            val doneDays = _workoutWeek.first()

            if (doneDays.contains(day)) {
                val previousExercises = workoutRepository.getWorkoutReviewExercises(day).first()
                _internalState.update { state ->
                    state.copy(
                        daySelected = day,
                        screenMode = ScreenMode.Review,
                        selectedExerciseIds = previousExercises.toMutableSet()
                    )
                }
                return@launch
            }

            _eventsChannel.trySend(
                ExerciseListEvent.Message(
                    reason = UiText.StringResource(id = R.string.not_trained_this_day)
                )
            )
        }
    }

    private fun toggleActiveWorkoutDialog(show: Boolean) {
        _internalState.update { state -> state.copy(activeWorkoutDialogVisible = show) }
    }

    private fun toggleDeletePlanDialog(show: Boolean) {
        _internalState.update { state -> state.copy(deletePlanDialogVisible = show) }
    }

    private fun toggleDeleteReviewDialog(show: Boolean) {
        _internalState.update { state -> state.copy(deleteDayWorkoutDialogVisible = show) }
    }

    private fun toggleMuscleSelection(muscleId: Int) {
        val newMuscles = _internalState.value.selectedMuscleIds.toMutableSet()

        if (newMuscles.contains(muscleId)) newMuscles.remove(muscleId)
        else newMuscles.add(muscleId)

        _internalState.update { state ->
            state.copy(selectedMuscleIds = newMuscles)
        }
    }

    private fun toggleExerciseSelection(exerciseId: Int) {
        // Do not let update exercises when reviewing previous days
        if (_internalState.value.screenMode is ScreenMode.Review) return

        _internalState.update { state ->
            val exercises = state.selectedExerciseIds.toMutableSet().apply {
                if (contains(exerciseId)) remove(exerciseId)
                else add(exerciseId)
            }
            state.copy(selectedExerciseIds = exercises)
        }
    }

    private fun addExercise(name: String) {
        val muscles = _internalState.value.selectedMuscleIds

        if (name.isBlank()) return
        if (muscles.size != 1) return

        addExercise(name, muscles.first())
        _internalState.update { state -> state.copy(exerciseDialogVisible = false) }
    }

    private fun addMuscle(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            muscleRepository.insert(Muscle(name = name))
            _internalState.update { state -> state.copy(muscleDialogVisible = false) }
        }
    }

    private fun addExercise(name: String, muscleId: Int) {
        viewModelScope.launch {
            exerciseRepository.insert(
                Exercise(name = name, targetMuscleId = muscleId)
            )
        }
    }

    private data class InternalState(
        val screenMode: ScreenMode = ScreenMode.Workout,
        val daySelected: Int = -1,
        val startWorkoutButtonVisible: Boolean = false,
        val muscleDialogVisible: Boolean = false,
        val exerciseDialogVisible: Boolean = false,
        val activeWorkoutDialogVisible: Boolean = false,
        val deletePlanDialogVisible: Boolean = false,
        val deleteDayWorkoutDialogVisible: Boolean = false,
        val plannedTodayDialogVisible: Boolean = false,
        val plannedExercisesForToday: List<Int> = emptyList(),
        val selectedMuscleIds: MutableSet<Int> = mutableSetOf(),
        val selectedExerciseIds: MutableSet<Int> = mutableSetOf()
    )
}
