package com.example.exercisetracker.domain.repository

import com.example.exercisetracker.domain.model.Muscle
import kotlinx.coroutines.flow.Flow

interface IMuscleRepository {
    fun allMuscles(): Flow<List<Muscle>>
    suspend fun insert(muscle: Muscle)
}
