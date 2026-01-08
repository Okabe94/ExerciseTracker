package com.example.exercisetracker.domain.repository

import com.example.exercisetracker.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface IExerciseRepository {
    val allExercises: Flow<List<Exercise>>
    fun getExercisesForMuscle(muscleId: Int): Flow<List<Exercise>>
    suspend fun insert(exercise: Exercise)
    suspend fun delete(exercise: Exercise)
}
