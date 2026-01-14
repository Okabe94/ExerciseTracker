package com.example.exercisetracker.domain.repository

import com.example.exercisetracker.domain.model.WorkoutSession
import com.example.exercisetracker.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

interface IWorkoutRepository {
    fun getLastActiveSessionSets(): Flow<List<WorkoutSet>>
    fun getLastActiveSessionFlow(): Flow<WorkoutSession?>
    fun getSetsForSession(sessionId: Int): Flow<List<WorkoutSet>>
    suspend fun updateSet(set: WorkoutSet)
    suspend fun getActiveSessionId(): Int?
    suspend fun completeOpenSessions()
    suspend fun startNewSession(exercises: List<Int>): WorkoutSession
    suspend fun getAllActiveSession(): List<WorkoutSession>?
    suspend fun saveSets(sessionId: Int, exerciseId: Int, sets: List<WorkoutSet>)
}
