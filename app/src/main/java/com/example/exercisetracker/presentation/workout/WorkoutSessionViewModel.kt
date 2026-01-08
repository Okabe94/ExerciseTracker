package com.example.exercisetracker.presentation.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.WorkoutSet
import com.example.exercisetracker.domain.repository.IExerciseRepository
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class UiWorkoutSet(
    val id: String = UUID.randomUUID().toString(),
    val weight: String = "",
    val reps: String = ""
)

class WorkoutSessionViewModel(
    private val exerciseRepository: IExerciseRepository,
    private val workoutRepository: IWorkoutRepository,
    private val exerciseIds: Set<Int>
) : ViewModel() {

    private var currentSessionId: Int? = null

    val exercises: StateFlow<List<Exercise>> = exerciseRepository.allExercises
        .map { list -> list.filter { it.id in exerciseIds } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _sets = MutableStateFlow<Map<Int, List<UiWorkoutSet>>>(emptyMap())
    val sets = _sets.asStateFlow()

    init {
        initializeSession()
    }

    private fun initializeSession() {
        viewModelScope.launch {
            // Check for existing active session
            val activeSession = workoutRepository.getActiveSession()
            
            if (activeSession != null) {
                currentSessionId = activeSession.id
                // Load existing sets
                val savedSets = workoutRepository.getSetsForSession(activeSession.id).first()
                val loadedSets = savedSets.groupBy { it.exerciseId }
                    .mapValues { (_, sets) ->
                        sets.map { 
                            UiWorkoutSet(
                                weight = if (it.weight == 0f) "" else it.weight.toString(),
                                reps = if (it.reps == 0) "" else it.reps.toString()
                            ) 
                        }
                    }
                _sets.value = loadedSets
            } else {
                // Start new session
                val newSession = workoutRepository.startNewSession()
                currentSessionId = newSession.id
            }
        }
    }

    fun addSet(exerciseId: Int) {
        val currentSets = _sets.value.toMutableMap()
        val exerciseSets = currentSets[exerciseId]?.toMutableList() ?: mutableListOf()
        exerciseSets.add(UiWorkoutSet())
        currentSets[exerciseId] = exerciseSets
        _sets.value = currentSets
        saveCurrentState()
    }

    fun updateSet(exerciseId: Int, setId: String, weight: String, reps: String) {
        val currentSets = _sets.value.toMutableMap()
        val exerciseSets = currentSets[exerciseId]?.toMutableList() ?: return
        val index = exerciseSets.indexOfFirst { it.id == setId }
        if (index != -1) {
            exerciseSets[index] = exerciseSets[index].copy(weight = weight, reps = reps)
            currentSets[exerciseId] = exerciseSets
            _sets.value = currentSets
            saveCurrentState()
        }
    }
    
    fun removeSet(exerciseId: Int, setId: String) {
        val currentSets = _sets.value.toMutableMap()
        val exerciseSets = currentSets[exerciseId]?.toMutableList() ?: return
        exerciseSets.removeAll { it.id == setId }
        currentSets[exerciseId] = exerciseSets
        _sets.value = currentSets
        saveCurrentState()
    }

    private fun saveCurrentState() {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch {
            val setsToSave = _sets.value.flatMap { (exerciseId, sets) ->
                sets.mapIndexed { index, set ->
                    WorkoutSet(
                        sessionId = sessionId,
                        exerciseId = exerciseId,
                        setNumber = index + 1,
                        weight = set.weight.toFloatOrNull() ?: 0f,
                        reps = set.reps.toIntOrNull() ?: 0
                    )
                }
            }
            workoutRepository.saveSets(sessionId, setsToSave)
        }
    }

    fun finishWorkout() {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch {
            // Ensure final state is saved
            saveCurrentState()
            workoutRepository.completeSession(sessionId)
        }
    }
}
