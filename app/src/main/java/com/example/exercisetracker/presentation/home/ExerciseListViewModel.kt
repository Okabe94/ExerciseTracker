package com.example.exercisetracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.repository.IExerciseRepository
import com.example.exercisetracker.domain.repository.IMuscleRepository
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExerciseListViewModel(
    private val muscleRepository: IMuscleRepository,
    private val exerciseRepository: IExerciseRepository,
    private val workoutRepository: IWorkoutRepository
) : ViewModel() {

    private val _internalState = MutableStateFlow(InternalState())
    private val _activeWorkout = workoutRepository.getLastActiveSessionFlow()

    val state: StateFlow<ExerciseListState> = combine(
        _internalState,
        _activeWorkout,
        muscleRepository.allMuscles,
        exerciseRepository.allExercises(),
    ) { internal, active, muscles, allExercises ->
        val filteredExercises = if (internal.selectedMuscleIds.isEmpty()) {
            allExercises
        } else {
            allExercises.filter { it.targetMuscleId in internal.selectedMuscleIds }
        }

        ExerciseListState(
            muscleList = muscles,
            exerciseList = filteredExercises,
            hasActiveWorkout = active != null,
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
            is ExerciseListAction.OnAddMuscle -> addMuscle(action.name)
            is ExerciseListAction.OnAddExercise -> addExercise(action.name)
            is ExerciseListAction.OnMuscleSelected -> toggleMuscleSelection(action.muscleId)
            is ExerciseListAction.OnExerciseSelected -> toggleExerciseSelection(action.exerciseId)

            is ExerciseListAction.OnShowExerciseDialog -> _internalState.update {
                it.copy(exerciseDialogVisible = action.show)
            }

            is ExerciseListAction.OnShowMuscleDialog -> _internalState.update {
                it.copy(muscleDialogVisible = action.show)
            }

            ExerciseListAction.OnStartWorkout -> {
                viewModelScope.launch {
                    toggleConfirmDialog(false)
                    workoutRepository.completeOpenSessions()
                    workoutRepository.startNewSession(
                        exercises = _internalState.value.selectedExerciseIds.toList()
                    )
                }
            }

            is ExerciseListAction.OnShowConfirmDialog -> toggleConfirmDialog(action.show)

            else -> Unit
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
        val startWorkoutButtonVisible: Boolean = false,
        val muscleDialogVisible: Boolean = false,
        val exerciseDialogVisible: Boolean = false,
        val confirmDialogVisible: Boolean = false,
        val selectedMuscleIds: MutableSet<Int> = mutableSetOf(),
        val selectedExerciseIds: MutableSet<Int> = mutableSetOf()
    )
}
