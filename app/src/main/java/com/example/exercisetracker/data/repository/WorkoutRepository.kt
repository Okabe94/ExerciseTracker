@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.exercisetracker.data.repository

import com.example.exercisetracker.data.local.dao.WorkoutDao
import com.example.exercisetracker.data.local.entity.WorkoutSessionEntity
import com.example.exercisetracker.data.local.model.MetricGraphData
import com.example.exercisetracker.data.local.model.WorkoutReview
import com.example.exercisetracker.data.mapper.toDomain
import com.example.exercisetracker.data.mapper.toEntity
import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.model.WorkoutSession
import com.example.exercisetracker.domain.model.WorkoutSet
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import com.example.exercisetracker.domain.time.AppClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoField

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val clock: AppClock
) : IWorkoutRepository {

    override suspend fun getAllActiveSession(): List<WorkoutSession>? =
        workoutDao.getAllActiveSession()?.map { it.toDomain() }

    override suspend fun getActiveSessionId(): Int? = workoutDao.getActiveSessionId()

    override fun getLastActiveSessionSets(): Flow<List<WorkoutSet>> {
        return workoutDao.getLastActiveSessionSets()
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getLastActiveSessionFlow(): Flow<WorkoutSession?> =
        workoutDao.getLastActiveSessionFlow()
            .map { it?.toDomain() }

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

    override fun getSetsForSession(sessionId: Int): Flow<List<WorkoutSet>> =
        workoutDao.getSetsForSession(sessionId)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun updateSet(set: WorkoutSet) {
        workoutDao.updateSet(set.toEntity())
    }

    override fun getWorkoutDays(): Flow<Set<Int>> {
        val zone = ZoneId.systemDefault()
        val thisMonday = Instant.ofEpochMilli(clock.now())
            .atZone(zone)
            .with(ChronoField.DAY_OF_WEEK, 1)
            .toLocalDate()
            .atStartOfDay()

        val nextMonday = thisMonday.plusWeeks(1)

        val thisMondayMillis = thisMonday.atZone(zone).toInstant().toEpochMilli()
        val nextMondayMillis = nextMonday.atZone(zone).toInstant().toEpochMilli()

        return workoutDao.getWorkoutDays(thisMondayMillis, nextMondayMillis)
            .map { list ->
                list.map { millis ->
                    Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate().dayOfWeek.value
                }.toSet()
            }
    }

    override fun getGraphData(
        timeFilter: TimeFilter,
        exerciseId: Int
    ): Flow<List<MetricGraphData>> {
        val now = clock.now()
        val then = clock.millisThen(timeFilter)

        return workoutDao.getGraphData(
            startTime = then,
            endTime = now,
            exerciseId = exerciseId
        )
    }

    override suspend fun getWorkoutReview(day: Int): List<WorkoutReview> {
        val zone = ZoneId.systemDefault()
        val startOfDay = Instant.ofEpochMilli(clock.now())
            .atZone(zone)
            .with(ChronoField.DAY_OF_WEEK, day.toLong())
            .toLocalDate()
            .atStartOfDay()

        val endOfDay = startOfDay.plusDays(1)

        val startMillis = startOfDay.atZone(zone).toInstant().toEpochMilli()
        val endMillis = endOfDay.atZone(zone).toInstant().toEpochMilli()

        return workoutDao.getWorkoutReview(startMillis, endMillis)
    }
}
