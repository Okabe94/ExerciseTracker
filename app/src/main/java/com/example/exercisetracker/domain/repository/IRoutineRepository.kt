package com.example.exercisetracker.domain.repository

import com.example.exercisetracker.domain.model.Routine
import kotlinx.coroutines.flow.Flow

interface IRoutineRepository {
    fun getAllRoutines(): Flow<List<Routine>>
    suspend fun insert(routine: Routine): Long
    suspend fun update(routine: Routine)
    suspend fun delete(id: Int)
}
