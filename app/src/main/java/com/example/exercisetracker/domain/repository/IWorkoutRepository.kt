package com.example.exercisetracker.domain.repository

import com.example.exercisetracker.domain.model.WorkoutSession
import com.example.exercisetracker.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

interface IWorkoutRepository {
    fun getActiveSessionFlow(): Flow<WorkoutSession?>
    fun getSetsForSession(sessionId: Int): Flow<List<WorkoutSet>>
    suspend fun completeSession(sessionId: Int)
    suspend fun startNewSession(): WorkoutSession
    suspend fun getActiveSession(): WorkoutSession?
    suspend fun saveSets(sessionId: Int, sets: List<WorkoutSet>)
}
