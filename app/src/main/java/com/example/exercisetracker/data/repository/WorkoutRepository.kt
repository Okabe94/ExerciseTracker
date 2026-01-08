package com.example.exercisetracker.data.repository

import com.example.exercisetracker.data.local.WorkoutDao
import com.example.exercisetracker.data.local.WorkoutSessionEntity
import com.example.exercisetracker.data.mapper.toDomain
import com.example.exercisetracker.data.mapper.toEntity
import com.example.exercisetracker.domain.model.WorkoutSession
import com.example.exercisetracker.domain.model.WorkoutSet
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepository(private val workoutDao: WorkoutDao) : IWorkoutRepository {
    
    override suspend fun getActiveSession(): WorkoutSession? {
        return workoutDao.getActiveSession()?.toDomain()
    }
    
    override fun getActiveSessionFlow(): Flow<WorkoutSession?> {
        return workoutDao.getActiveSessionFlow()
            .map { it?.toDomain() }
    }

    override suspend fun startNewSession(): WorkoutSession {
        val existingSession = workoutDao.getActiveSession()
        if (existingSession != null) {
            return existingSession.toDomain()
        }
        
        val newSession = WorkoutSessionEntity(
            startTime = System.currentTimeMillis(),
            endTime = null,
            isCompleted = false
        )
        val id = workoutDao.insertSession(newSession)
        return newSession.copy(id = id.toInt()).toDomain()
    }

    override suspend fun completeSession(sessionId: Int) {
        val session = workoutDao.getActiveSession() ?: return
        if (session.id == sessionId) {
            workoutDao.updateSession(
                session.copy(
                    endTime = System.currentTimeMillis(),
                    isCompleted = true
                )
            )
        }
    }

    override suspend fun saveSets(sessionId: Int, sets: List<WorkoutSet>) {
        workoutDao.clearSetsForSession(sessionId)
        workoutDao.insertSets(sets.map { it.toEntity() })
    }

    override fun getSetsForSession(sessionId: Int): Flow<List<WorkoutSet>> {
        return workoutDao.getSetsForSession(sessionId)
            .map { list -> list.map { it.toDomain() } }
    }
}
