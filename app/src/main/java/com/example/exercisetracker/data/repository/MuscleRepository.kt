package com.example.exercisetracker.data.repository

import com.example.exercisetracker.data.local.dao.MuscleDao
import com.example.exercisetracker.data.mapper.toDomain
import com.example.exercisetracker.data.mapper.toEntity
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.repository.IMuscleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MuscleRepository(private val muscleDao: MuscleDao) : IMuscleRepository {

    override fun allMuscles(): Flow<List<Muscle>> = muscleDao.getAllMuscles()
        .map { list -> list.map { it.toDomain() } }

    override suspend fun insert(muscle: Muscle) {
        muscleDao.insert(muscle.toEntity())
    }
}
