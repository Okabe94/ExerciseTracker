package com.example.exercisetracker.data.repository

import com.example.exercisetracker.data.local.dao.WorkoutPlanDao
import com.example.exercisetracker.data.mapper.toDomain
import com.example.exercisetracker.data.mapper.toEntity
import com.example.exercisetracker.domain.model.WorkoutPlan
import com.example.exercisetracker.domain.repository.IWorkoutPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutPlanRepository(private val workoutPlanDao: WorkoutPlanDao) : IWorkoutPlanRepository {
    override fun getAllWorkoutPlans(): Flow<List<WorkoutPlan>> {
        return workoutPlanDao.getAllWorkoutPlans().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getWorkoutPlanById(id: Int): WorkoutPlan? {
        return workoutPlanDao.getWorkoutPlanById(id)?.toDomain()
    }

    override suspend fun insertWorkoutPlan(workoutPlan: WorkoutPlan): Long {
        return workoutPlanDao.insertWorkoutPlan(workoutPlan.toEntity())
    }

    override suspend fun updateWorkoutPlan(workoutPlan: WorkoutPlan) {
        workoutPlanDao.updateWorkoutPlan(workoutPlan.toEntity())
    }

    override suspend fun deleteWorkoutPlan(workoutPlan: WorkoutPlan) {
        workoutPlanDao.deleteWorkoutPlan(workoutPlan.toEntity())
    }
}
