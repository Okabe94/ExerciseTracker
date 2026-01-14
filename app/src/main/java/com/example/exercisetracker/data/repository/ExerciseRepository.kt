package com.example.exercisetracker.data.repository

import com.example.exercisetracker.data.local.dao.ExerciseDao
import com.example.exercisetracker.data.mapper.toDomain
import com.example.exercisetracker.data.mapper.toEntity
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.repository.IExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepository(private val exerciseDao: ExerciseDao) : IExerciseRepository {

    override val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()
        .map { list -> list.map { it.toDomain() } }
    
    override fun getExercisesForMuscle(muscleId: Int): Flow<List<Exercise>> {
        return exerciseDao.getExercisesForMuscle(muscleId)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insert(exercise: Exercise) {
        exerciseDao.insertExercise(exercise.toEntity())
    }

    override suspend fun delete(exercise: Exercise) {
        exerciseDao.deleteExercise(exercise.toEntity())
    }
}
