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

        ExerciseListState(
            screenMode = internal.screenMode,
            workoutDaysDone = daysDone,
            plannedWorkouts = plannedWeek,
            currentDay = clock.getCurrentNumberDay(),
            muscleList = musclesAndExercises.first,
            exerciseList = filteredExercises,
            hasActiveWorkout = activeWorkout != null,
            confirmDialogVisible = internal.confirmDialogVisible,
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
            ExerciseListAction.OnSaveWorkout -> saveWorkout()
            ExerciseListAction.OnStartWorkout -> startWorkout()
            is ExerciseListAction.OnAddMuscle -> addMuscle(action.name)
            is ExerciseListAction.OnAddExercise -> addExercise(action.name)
            is ExerciseListAction.OnDayNodeSelected -> dayNodeAction(action.day)
            is ExerciseListAction.OnShowConfirmDialog -> toggleConfirmDialog(action.show)
            is ExerciseListAction.OnMuscleSelected -> toggleMuscleSelection(action.muscleId)
            is ExerciseListAction.OnExerciseSelected -> toggleExerciseSelection(action.exerciseId)

            ExerciseListAction.OnReturnToWorkout -> _internalState.update { state ->
                state.copy(
                    screenMode = ScreenMode.Workout,
                    selectedExerciseIds = mutableSetOf(),
                    selectedMuscleIds = mutableSetOf()
                )
            }

            is ExerciseListAction.OnShowExerciseDialog -> _internalState.update {
                it.copy(exerciseDialogVisible = action.show)
            }

            is ExerciseListAction.OnShowMuscleDialog -> _internalState.update {
                it.copy(muscleDialogVisible = action.show)
            }

            else -> Unit
        }
    }

    private fun saveWorkout() {
        val mode = _internalState.value.screenMode
        if (mode is ScreenMode.Planning) {
            viewModelScope.launch {
                planRepository.insertWorkoutPlan(
                    WorkoutPlan(
                        day = mode.day,
                        exercises = _internalState.value.selectedExerciseIds.toList()
                    )
                )
            }
            _internalState.update { state ->
                state.copy(
                    screenMode = ScreenMode.Workout,
                    selectedExerciseIds = mutableSetOf(),
                    selectedMuscleIds = mutableSetOf()
                )
            }

            _eventsChannel.trySend(ExerciseListEvent.WorkoutSaved)
        }
    }

    private fun startWorkout() {
        viewModelScope.launch {
            toggleConfirmDialog(false)
            workoutRepository.completeOpenSessions()
            workoutRepository.startNewSession(
                exercises = _internalState.value.selectedExerciseIds.toList()
            )
        }
    }

    private fun dayNodeAction(day: Int) {
        val currentDay = clock.getCurrentNumberDay()

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
                            selectedExerciseIds = planned
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
                _eventsChannel.trySend(ExerciseListEvent.SendToReview(day))
                return@launch
            }

            _eventsChannel.trySend(
                ExerciseListEvent.ErrorMessage(
                    reason = UiText.StringResource(id = R.string.not_trained_this_day)
                )
            )
        }
    }

    private fun toggleConfirmDialog(show: Boolean) {
        _internalState.update { state -> state.copy(confirmDialogVisible = show) }
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
        val startWorkoutButtonVisible: Boolean = false,
        val muscleDialogVisible: Boolean = false,
        val exerciseDialogVisible: Boolean = false,
        val confirmDialogVisible: Boolean = false,
        val selectedMuscleIds: MutableSet<Int> = mutableSetOf(),
        val selectedExerciseIds: MutableSet<Int> = mutableSetOf()
    )
}
