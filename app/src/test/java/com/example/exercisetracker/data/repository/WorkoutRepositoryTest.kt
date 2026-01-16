package com.example.exercisetracker.data.repository

import app.cash.turbine.test
import com.example.exercisetracker.data.local.dao.WorkoutDao
import com.example.exercisetracker.data.local.entity.WorkoutSessionEntity
import com.example.exercisetracker.data.local.entity.WorkoutSetEntity
import com.example.exercisetracker.data.mapper.toEntity
import com.example.exercisetracker.domain.model.WorkoutSet
import com.example.exercisetracker.domain.time.AppClock
import com.example.exercisetracker.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var workoutRepository: WorkoutRepository
    private val workoutDao: WorkoutDao = mock()
    private val clock: AppClock = mock()

    @Before
    fun setUp() {
        workoutRepository = WorkoutRepository(workoutDao, clock)
    }

    @Test
    fun `getAllActiveSession should return active sessions`() = runTest {
        val activeSessions = listOf(WorkoutSessionEntity(1, 0L, null, emptyList(), false))
        Mockito.`when`(workoutDao.getAllActiveSession()).thenReturn(activeSessions)

        val result = workoutRepository.getAllActiveSession()

        assertNotNull(result)
        assertEquals(1, result?.size)
    }

    @Test
    fun `getActiveSessionId should return an id`() = runTest {
        val sessionId = 1
        Mockito.`when`(workoutDao.getActiveSessionId()).thenReturn(sessionId)

        val result = workoutRepository.getActiveSessionId()

        assertEquals(sessionId, result)
    }

    @Test
    fun `getLastActiveSessionSets should return sets`() = runTest {
        val sets = listOf(WorkoutSetEntity(1, 1, 1, 1, 10f, 10))
        Mockito.`when`(workoutDao.getLastActiveSessionSets()).thenReturn(flowOf(sets))

        workoutRepository.getLastActiveSessionSets().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getLastActiveSessionFlow should return a session`() = runTest {
        val session = WorkoutSessionEntity(1, 0L, null, emptyList(), false)
        Mockito.`when`(workoutDao.getLastActiveSessionFlow()).thenReturn(flowOf(session))

        workoutRepository.getLastActiveSessionFlow().test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals(1, result?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `startNewSession should insert a new session`() = runTest {
        val exercises = listOf(1, 2, 3)
        Mockito.`when`(workoutDao.insertSession(org.mockito.kotlin.any()))
            .thenReturn(1L)

        Mockito.`when`(clock.now())
            .thenReturn(1L)

        val result = workoutRepository.startNewSession(exercises)

        assertEquals(1, result.id)
        assertEquals(exercises, result.exercises)
    }

    @Test
    fun `completeOpenSessions should update open sessions`() = runTest {
        val openSessions = listOf(
            WorkoutSessionEntity(
                id = 1,
                startTime = 0L,
                endTime = null,
                exercises = emptyList(),
                isCompleted = false
            )
        )

        Mockito.`when`(clock.now())
            .thenReturn(1L)

        Mockito.`when`(workoutDao.getAllActiveSession())
            .thenReturn(openSessions)

        workoutRepository.completeOpenSessions()

        verify(workoutDao).updateSession(
            openSessions.first().copy(
                endTime = clock.now(),
                isCompleted = true
            )
        )
    }

    @Test
    fun `saveSets should clear and insert sets`() = runTest {
        val sessionId = 1
        val exerciseId = 1
        val sets = listOf(
            WorkoutSet(
                id = 1,
                sessionId = sessionId,
                exerciseId = exerciseId,
                setNumber = 1,
                weight = 10f,
                reps = 10
            )
        )

        workoutRepository.saveSets(
            sessionId = sessionId,
            exerciseId = exerciseId,
            sets = sets
        )

        verify(workoutDao).clearExerciseSets(sessionId = sessionId, exerciseId = exerciseId)
        verify(workoutDao).insertSets(org.mockito.kotlin.any())
    }

    @Test
    fun `getSetsForSession should return sets`() = runTest {
        val sessionId = 1
        val sets = listOf(WorkoutSetEntity(1, sessionId, 1, 1, 10f, 10))
        Mockito.`when`(workoutDao.getSetsForSession(sessionId)).thenReturn(flowOf(sets))

        workoutRepository.getSetsForSession(sessionId).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateSet should call updateSet`() = runTest {
        val set = WorkoutSet(1, 1, 1, 1, 10f, 10)
        workoutRepository.updateSet(set)
        verify(workoutDao).updateSet(set.toEntity())
    }
}