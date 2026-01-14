package com.example.exercisetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.exercisetracker.data.local.entity.MuscleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MuscleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(muscles: List<MuscleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(muscle: MuscleEntity)

    @Query("SELECT * FROM muscles")
    fun getAllMuscles(): Flow<List<MuscleEntity>>
}
