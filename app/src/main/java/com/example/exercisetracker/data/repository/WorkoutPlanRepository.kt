package com.example.exercisetracker.data.repository

import com.example.exercisetracker.data.local.dao.WorkoutPlanDao
import com.example.exercisetracker.data.mapper.toDomain
import com.example.exercisetracker.data.mapper.toEntity
import com.example.exercisetracker.domain.model.WorkoutPlan
import com.example.exercisetracker.domain.repository.IWorkoutPlanRepository
import com.example.exercisetracker.domain.time.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutPlanRepository(
    private val clock: AppClock,
    private val workoutPlanDao: WorkoutPlanDao
) : IWorkoutPlanRepository {

    override fun getPlannedWorkouts(): Flow<List<WorkoutPlan>> =
        workoutPlanDao.getWeekWorkoutPlans(
            clock.getMillisForStartOfDayInWeek(clock.getCurrentDayOfWeek() + 1)
        ).map { list ->
            list.map {
                it.toDomain(clock.getDayOfWeekFromMillis(it.date))
            }
        }

    override suspend fun insertWorkoutPlan(workoutPlan: WorkoutPlan): Long =
        workoutPlanDao.insertWorkoutPlan(
            workoutPlan.toEntity(
                millis = clock.getMillisForStartOfDayInWeek(workoutPlan.day)
            )
        )

    override suspend fun updateWorkoutPlan(day: Int, newExercises: List<Int>) {
        val starMillis = clock.getMillisForStartOfDayInWeek(day)
        val endMillis = clock.getMillisForStartOfDayInWeek(day + 1)
        workoutPlanDao.updateWorkoutPlan(
            startDate = starMillis,
            endDate = endMillis,
            newExercises = newExercises
        )
    }

    override suspend fun deleteWorkoutPlan(workoutPlan: WorkoutPlan) =
        workoutPlanDao.deleteWorkoutPlan(
            workoutPlan.toEntity(
                millis = clock.getMillisForStartOfDayInWeek(workoutPlan.day)
            )
        )

    override suspend fun deleteWorkoutPlanFromDay(day: Int) {
        val starMillis = clock.getMillisForStartOfDayInWeek(day)
        val endMillis = clock.getMillisForStartOfDayInWeek(day + 1)
        workoutPlanDao.deleteWorkoutPlan(starMillis, endMillis)
    }
}
