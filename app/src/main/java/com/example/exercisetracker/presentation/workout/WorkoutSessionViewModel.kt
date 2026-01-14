@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.exercisetracker.presentation.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercisetracker.domain.model.WorkoutSet
import com.example.exercisetracker.domain.repository.IExerciseRepository
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutSessionViewModel(
    private val exerciseRepository: IExerciseRepository,
    private val workoutRepository: IWorkoutRepository,
) : ViewModel() {

    val state = combine(
        workoutRepository.getLastActiveSessionFlow(),
        exerciseRepository.allExercises,
        workoutRepository.getLastActiveSessionSets(),
    ) { sessionInfo, allExercises, sessionSets ->

        val sessionExercises = allExercises.filter {
            it.id in (sessionInfo?.exercises ?: emptyList())
        }

        val groupedSets = sessionSets.groupBy { it.exerciseId }
            .mapValues { (_, sets) ->
                sets.map {
                    WorkoutSessionSet(
                        id = it.id,
                        number = it.setNumber,
                        weight = if (it.weight == 0f) "" else it.weight.toString(),
                        reps = if (it.reps == 0) "" else it.reps.toString()
                    )
                }
            }

        val listOfExercises = sessionExercises.map {
            WorkoutSessionExercise(
                name = it.name,
                id = it.id,
                sets = groupedSets[it.id] ?: emptyList()
            )
        }

        WorkoutSessionState(
            sessionId = sessionInfo?.id ?: 0,
            listOfExercises = listOfExercises
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = WorkoutSessionState()
    )

    fun onAction(action: WorkoutSessionAction) {
        when (action) {
            is WorkoutSessionAction.OnAddSet -> addSet(action.exerciseId)
            WorkoutSessionAction.OnFinishWorkout -> finishWorkout()
            is WorkoutSessionAction.OnRemoveSet -> removeSet(
                exerciseId = action.exerciseId,
                setId = action.setId
            )

            is WorkoutSessionAction.OnUpdateSet -> updateSet(
                exerciseId = action.exerciseId,
                setId = action.setId,
                weight = action.weight,
                reps = action.reps
            )
        }
    }

    private fun addSet(exerciseId: Int) {
        val sessionId = state.value.sessionId
        val exercise = state.value.listOfExercises.firstOrNull { it.id == exerciseId } ?: return

        val sets = exercise.sets.toMutableList().apply { add(WorkoutSessionSet()) }
        val mapped = sets.mapIndexed { index, set ->
            WorkoutSet(
                sessionId = sessionId,
                exerciseId = exerciseId,
                setNumber = index + 1,
                weight = set.weight.toFloatOrNull() ?: 0f,
                reps = set.reps.toIntOrNull() ?: 0
            )
        }
        viewModelScope.launch {
            workoutRepository.saveSets(
                sessionId = sessionId,
                exerciseId = exerciseId,
                sets = mapped
            )
        }
    }

    fun updateSet(exerciseId: Int, setId: Int, weight: String, reps: String) {
        val sessionId = state.value.sessionId
        val sets = state.value.listOfExercises.firstOrNull { it.id == exerciseId } ?: return
        val set = sets.sets.firstOrNull { it.id == setId } ?: return

        val updatedSet = WorkoutSet(
            id = set.id,
            sessionId = sessionId,
            exerciseId = exerciseId,
            setNumber = set.number,
            weight = weight.toFloatOrNull() ?: 0f,
            reps = reps.toIntOrNull() ?: 0
        )

        viewModelScope.launch {
            workoutRepository.updateSet(updatedSet)
        }
    }

    fun removeSet(exerciseId: Int, setId: Int) {
        val sessionId = state.value.sessionId
        val exercise = state.value.listOfExercises.firstOrNull { it.id == exerciseId } ?: return
        val updatedSet = exercise.sets.filter { it.id != setId }.mapIndexed { index, set ->
            WorkoutSet(
                sessionId = sessionId,
                exerciseId = exercise.id,
                setNumber = index + 1,
                weight = set.weight.toFloatOrNull() ?: 0f,
                reps = set.reps.toIntOrNull() ?: 0
            )
        }

        viewModelScope.launch {
            workoutRepository.saveSets(
                sessionId = sessionId,
                exerciseId = exerciseId,
                sets = updatedSet
            )
        }
    }

    private fun finishWorkout() {
        viewModelScope.launch {
            val sessionId = state.value.sessionId
            val exercises = state.value.listOfExercises
            exercises.forEach { exercise ->
                val hasEmptySets = exercise.sets.any {
                    it.weight.isBlank() && it.reps.isBlank()
                }

                val finishedSets = exercise.sets.filterNot {
                    it.weight.isBlank() && it.reps.isBlank()
                }

                if (hasEmptySets) {
                    val mappedSets = finishedSets.mapIndexed { index, set ->
                        WorkoutSet(
                            sessionId = sessionId,
                            exerciseId = exercise.id,
                            setNumber = index + 1,
                            weight = set.weight.toFloatOrNull() ?: 0f,
                            reps = set.reps.toIntOrNull() ?: 0
                        )
                    }
                    workoutRepository.saveSets(
                        sessionId = sessionId,
                        exerciseId = exercise.id,
                        sets = mappedSets
                    )
                }

                workoutRepository.completeOpenSessions()
            }
        }
    }
}
