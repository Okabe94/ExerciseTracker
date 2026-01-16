package com.example.exercisetracker.data.repository

import com.example.exercisetracker.data.local.dao.WorkoutDao
import com.example.exercisetracker.data.local.entity.WorkoutSessionEntity
import com.example.exercisetracker.data.mapper.toDomain
import com.example.exercisetracker.data.mapper.toEntity
import com.example.exercisetracker.domain.model.WorkoutSession
import com.example.exercisetracker.domain.model.WorkoutSet
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import com.example.exercisetracker.domain.time.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val clock: AppClock
) : IWorkoutRepository {

    override suspend fun getAllActiveSession(): List<WorkoutSession>? {
        return workoutDao.getAllActiveSession()?.map { it.toDomain() }
    }

    override suspend fun getActiveSessionId(): Int? = workoutDao.getActiveSessionId()

    override fun getLastActiveSessionSets(): Flow<List<WorkoutSet>> {
        return workoutDao.getLastActiveSessionSets()
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getLastActiveSessionFlow(): Flow<WorkoutSession?> {
        return workoutDao.getLastActiveSessionFlow()
            .map { it?.toDomain() }
    }

    override suspend fun startNewSession(exercises: List<Int>): WorkoutSession {
        val newSession = WorkoutSessionEntity(
            startTime = clock.now(),
            endTime = null,
            exercises = exercises,
            isCompleted = false
        )
        val id = workoutDao.insertSession(newSession)
        return newSession.copy(id = id.toInt()).toDomain()
    }

    override suspend fun completeOpenSessions() {
        workoutDao.getAllActiveSession()?.forEach {
            workoutDao.updateSession(
                it.copy(
                    endTime = clock.now(),
                    isCompleted = true
                )
            )
        }
    }

    override suspend fun saveSets(sessionId: Int, exerciseId: Int, sets: List<WorkoutSet>) {
        workoutDao.clearExerciseSets(sessionId, exerciseId)
        workoutDao.insertSets(sets.map { it.toEntity() })
    }

    override fun getSetsForSession(sessionId: Int): Flow<List<WorkoutSet>> {
        return workoutDao.getSetsForSession(sessionId)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun updateSet(set: WorkoutSet) {
        workoutDao.updateSet(set.toEntity())
    }
}
