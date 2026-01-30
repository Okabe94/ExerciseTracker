package com.example.exercisetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.exercisetracker.data.local.entity.WorkoutPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {
    @Query("SELECT * FROM workout_plan")
    fun getAllWorkoutPlans(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plan WHERE id = :id")
    suspend fun getWorkoutPlanById(id: Int): WorkoutPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlan(workoutPlan: WorkoutPlanEntity): Long

    @Update
    suspend fun updateWorkoutPlan(workoutPlan: WorkoutPlanEntity)

    @Delete
    suspend fun deleteWorkoutPlan(workoutPlan: WorkoutPlanEntity)
}
