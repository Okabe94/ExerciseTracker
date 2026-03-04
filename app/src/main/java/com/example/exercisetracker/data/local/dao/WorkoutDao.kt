package com.example.exercisetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.exercisetracker.data.local.entity.WorkoutSessionEntity
import com.example.exercisetracker.data.local.entity.WorkoutSetEntity
import com.example.exercisetracker.data.local.model.MetricGraphData
import com.example.exercisetracker.data.local.model.ReviewExercises
import com.example.exercisetracker.data.local.model.WorkoutReview
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<WorkoutSetEntity>)

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Delete
    suspend fun deleteSet(set: WorkoutSetEntity)

    @Query("SELECT id FROM workout_sessions WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSessionId(): Int?

    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 0")
    suspend fun getAllActiveSession(): List<WorkoutSessionEntity>?

    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    fun getLastActiveSessionFlow(): Flow<WorkoutSessionEntity?>

    @Query("DELETE FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun clearSetsForSession(sessionId: Int)

    @Query("DELETE FROM workout_sets WHERE sessionId = :sessionId AND exerciseId = :exerciseId")
    suspend fun clearExerciseSets(sessionId: Int, exerciseId: Int)

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    fun getSetsForSession(sessionId: Int): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun getSetsForSessionSync(sessionId: Int): List<WorkoutSetEntity>

    @Query(
        "SELECT session.id AS sessionId, sets.id AS setId, startTime, setNumber, muscles.name AS muscleName, exercises.name AS exerciseName, weight, reps\n" +
                "FROM workout_sessions AS session\n" +
                "INNER JOIN workout_sets AS sets ON session.id = sets.sessionId\n" +
                "INNER JOIN exercises ON exerciseId = exercises.id\n" +
                "INNER JOIN muscles ON exercises.targetMuscleId = muscles.id\n" +
                "WHERE isCompleted = 1 AND startTime >= :startTime AND startTime <= :endTime\n" +
                "ORDER BY setNumber AND exerciseId ASC"
    )
    fun getWorkoutReview(startTime: Long, endTime: Long): Flow<List<WorkoutReview>>

    @Query("SELECT exercises FROM workout_sessions WHERE startTime >= :startTime AND startTime <= :endTime")
    fun getWorkoutReviewExercises(startTime: Long, endTime: Long): Flow<List<ReviewExercises>>

    @Query("SELECT * FROM workout_sets WHERE sessionId = (SELECT id FROM workout_sessions WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1)")
    fun getLastActiveSessionSets(): Flow<List<WorkoutSetEntity>>

    @Query("SELECT startTime FROM workout_sessions WHERE isCompleted = 1 AND endTime IS NOT NULL AND startTime BETWEEN :firstDay AND :lastDay")
    fun getWorkoutDays(firstDay: Long, lastDay: Long): Flow<List<Long>>

    @Query("SELECT workout_sets.id, startTime, weight, reps FROM workout_sessions INNER JOIN workout_sets ON workout_sessions.id = workout_sets.sessionId WHERE isCompleted = 1 AND startTime BETWEEN :startTime AND :endTime AND exerciseId = :exerciseId ORDER BY startTime ASC")
    fun getGraphData(startTime: Long, endTime: Long, exerciseId: Int): Flow<List<MetricGraphData>>

    @Query("DELETE FROM workout_sessions WHERE startTime >= :startTime AND startTime <= :endTime ")
    suspend fun deleteWorkoutSession(startTime: Long, endTime: Long)
}
