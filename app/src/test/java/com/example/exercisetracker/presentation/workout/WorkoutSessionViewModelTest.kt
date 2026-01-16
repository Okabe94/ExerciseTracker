package com.example.exercisetracker.presentation.workout

import app.cash.turbine.test
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.WorkoutSession
import com.example.exercisetracker.domain.model.WorkoutSet
import com.example.exercisetracker.domain.repository.IExerciseRepository
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import com.example.exercisetracker.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutSessionViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var workoutSessionViewModel: WorkoutSessionViewModel
    private val exerciseRepository: IExerciseRepository = mock()
    private val workoutRepository: IWorkoutRepository = mock()

    @Before
    fun setUp() {
        workoutSessionViewModel = WorkoutSessionViewModel(
            exerciseRepository = exerciseRepository,
            workoutRepository = workoutRepository
        )
    }

    @Test
    fun `addSet should save a new set`() = runTest {
        val sessionId = 1
        val exerciseId = 101
        val initialSets = emptyList<WorkoutSet>()
        val session = WorkoutSession(
            id = sessionId,
            startTime = 0L,
            endTime = 0L,
            exercises = listOf(exerciseId),
            isCompleted = false
        )
        val exercises = listOf(
            Exercise(id = exerciseId, targetMuscleId = 0, name = "Test Exercise")
        )

        Mockito.`when`(workoutRepository.getLastActiveSessionFlow())
            .thenReturn(flowOf(session))

        Mockito.`when`(exerciseRepository.allExercises())
            .thenReturn(flowOf(exercises))

        Mockito.`when`(workoutRepository.getLastActiveSessionSets())
            .thenReturn(flowOf(initialSets))

        workoutSessionViewModel.state.test {
            awaitItem() // Initial state
            workoutSessionViewModel.onAction(WorkoutSessionAction.OnAddSet(exerciseId))

            val expectedSet = WorkoutSet(
                sessionId = sessionId,
                exerciseId = exerciseId,
                setNumber = 1,
                weight = 0f,
                reps = 0
            )

            verify(workoutRepository).saveSets(
                sessionId = sessionId,
                exerciseId = exerciseId,
                sets = listOf(expectedSet)
            )
        }
    }

    @Test
    fun `updateSet should update the correct set`() = runTest {
        val sessionId = 1
        val exerciseId = 101
        val setId = 201
        val initialSet = WorkoutSet(
            id = setId,
            sessionId = sessionId,
            exerciseId = exerciseId,
            setNumber = 1,
            weight = 10f,
            reps = 10
        )
        val session = WorkoutSession(
            id = sessionId,
            startTime = 0L,
            endTime = 0L,
            exercises = listOf(exerciseId),
            isCompleted = false
        )
        val exercises = listOf(
            Exercise(id = exerciseId, targetMuscleId = 0, name = "Test Exercise")
        )

        Mockito.`when`(workoutRepository.getLastActiveSessionFlow())
            .thenReturn(flowOf(session))

        Mockito.`when`(exerciseRepository.allExercises())
            .thenReturn(flowOf(exercises))

        Mockito.`when`(workoutRepository.getLastActiveSessionSets())
            .thenReturn(flowOf(listOf(initialSet)))

        workoutSessionViewModel.state.test {
            awaitItem() // Initial state
            workoutSessionViewModel.onAction(
                WorkoutSessionAction.OnUpdateSet(
                    exerciseId,
                    setId,
                    "12",
                    "15"
                )
            )

            val expectedSet = initialSet.copy(weight = 12f, reps = 15)
            verify(workoutRepository).updateSet(expectedSet)
        }
    }

    @Test
    fun `removeSet should remove the correct set`() = runTest {
        val sessionId = 1
        val exerciseId = 101
        val setIdToRemove = 201
        val initialSets = listOf(
            WorkoutSet(
                id = setIdToRemove,
                sessionId = sessionId,
                exerciseId = exerciseId,
                setNumber = 1,
                weight = 10f,
                reps = 10
            ),
            WorkoutSet(
                id = 202,
                sessionId = sessionId,
                exerciseId = exerciseId,
                setNumber = 2,
                weight = 0f,
                reps = 0
            )
        )
        val session = WorkoutSession(
            id = sessionId,
            startTime = 0L,
            endTime = 0L,
            exercises = listOf(exerciseId),
            isCompleted = false
        )
        val exercises = listOf(
            Exercise(id = exerciseId, targetMuscleId = 0, name = "Test Exercise")
        )

        Mockito.`when`(workoutRepository.getLastActiveSessionFlow())
            .thenReturn(flowOf(session))

        Mockito.`when`(exerciseRepository.allExercises())
            .thenReturn(flowOf(exercises))

        Mockito.`when`(workoutRepository.getLastActiveSessionSets())
            .thenReturn(flowOf(initialSets))

        workoutSessionViewModel.state.test {
            awaitItem() // Initial state
            workoutSessionViewModel.onAction(
                WorkoutSessionAction.OnRemoveSet(
                    exerciseId,
                    setIdToRemove
                )
            )

            val expectedSets = listOf(
                WorkoutSet(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    setNumber = 1,
                    weight = 0f,
                    reps = 0
                )
            )
            verify(workoutRepository).saveSets(sessionId, exerciseId, expectedSets)
        }
    }

    @Test
    fun `finishWorkout should complete the session`() = runTest {
        val sessionId = 1
        val exerciseId = 101
        val sets = listOf(
            WorkoutSet(
                id = 201,
                sessionId = sessionId,
                exerciseId = exerciseId,
                setNumber = 1,
                weight = 10f,
                reps = 10
            ),
            WorkoutSet(
                id = 202,
                sessionId = sessionId,
                exerciseId = exerciseId,
                setNumber = 2,
                weight = 0f,
                reps = 0
            ) // Empty set
        )
        val session = WorkoutSession(
            id = sessionId,
            startTime = 0L,
            endTime = 0L,
            exercises = listOf(exerciseId),
            isCompleted = false
        )
        val exercises = listOf(
            Exercise(id = exerciseId, targetMuscleId = 0, name = "Test Exercise")
        )

        Mockito.`when`(workoutRepository.getLastActiveSessionFlow())
            .thenReturn(flowOf(session))

        Mockito.`when`(exerciseRepository.allExercises())
            .thenReturn(flowOf(exercises))

        Mockito.`when`(workoutRepository.getLastActiveSessionSets())
            .thenReturn(flowOf(sets))

        workoutSessionViewModel.state.test {
            awaitItem() // Initial state
            workoutSessionViewModel.onAction(WorkoutSessionAction.OnFinishWorkout)

            val expectedSets = listOf(sets.first())
            verify(workoutRepository).saveSets(sessionId, exerciseId, expectedSets)
            verify(workoutRepository).completeOpenSessions()
        }
    }
}