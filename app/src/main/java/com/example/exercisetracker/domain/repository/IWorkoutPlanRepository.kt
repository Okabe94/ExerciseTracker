package com.example.exercisetracker.domain.repository

import com.example.exercisetracker.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

interface IWorkoutPlanRepository {
    fun getPlannedWorkouts(): Flow<List<WorkoutPlan>>
    suspend fun insertWorkoutPlan(workoutPlan: WorkoutPlan): Long
    suspend fun updateWorkoutPlan(workoutPlan: WorkoutPlan)
    suspend fun deleteWorkoutPlan(workoutPlan: WorkoutPlan)
}
