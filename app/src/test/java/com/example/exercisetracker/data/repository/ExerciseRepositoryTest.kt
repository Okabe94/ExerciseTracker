package com.example.exercisetracker.data.repository

import app.cash.turbine.test
import com.example.exercisetracker.data.local.dao.ExerciseDao
import com.example.exercisetracker.data.local.entity.ExerciseEntity
import com.example.exercisetracker.data.mapper.toEntity
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var exerciseRepository: ExerciseRepository
    private val exerciseDao: ExerciseDao = mock()

    @Before
    fun setUp() {
        exerciseRepository = ExerciseRepository(exerciseDao)
    }

    @Test
    fun `allExercises should return a list of exercises`() = runTest {
        val exerciseEntities = listOf(ExerciseEntity(1, "Bench Press", 0))
        Mockito.`when`(exerciseDao.getAllExercises())
            .thenReturn(flowOf(exerciseEntities))

        exerciseRepository.allExercises().test {
            val exercises = awaitItem()
            assertEquals(1, exercises.size, )
            assertEquals("Bench Press", exercises.first().name)
            cancelAndIgnoreRemainingEvents()
        }

//        val exercises = exerciseDao.getAllExercises().first()
//        assertEquals(1, exercises.size)
//        assertEquals("Bench Press", exercises.first().name)
    }

    @Test
    fun `getExercisesForMuscle should return a list of exercises`() = runTest {
        val muscleId = 1
        val exerciseEntities = listOf(
            ExerciseEntity(
                id = 1,
                name = "Bicep Curl",
                targetMuscleId = 0
            )
        )
        Mockito.`when`(exerciseDao.getExercisesForMuscle(muscleId))
            .thenReturn(flowOf(exerciseEntities))

        exerciseRepository.getExercisesForMuscle(muscleId).test {
            val exercises = awaitItem()
            assertEquals(1, exercises.size)
            assertEquals("Bicep Curl", exercises.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insert should call insertExercise`() = runTest {
        val exercise = Exercise(name = "New Exercise", id = 0, targetMuscleId = 0)
        exerciseRepository.insert(exercise)
        verify(exerciseDao).insertExercise(exercise.toEntity())
    }

    @Test
    fun `delete should call deleteExercise`() = runTest {
        val exercise = Exercise(name = "Exercise to Delete", id = 1, targetMuscleId = 0)
        exerciseRepository.delete(exercise)
        verify(exerciseDao).deleteExercise(exercise.toEntity())
    }
}