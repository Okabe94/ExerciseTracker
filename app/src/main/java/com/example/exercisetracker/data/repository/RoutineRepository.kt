package com.example.exercisetracker.data.repository

import com.example.exercisetracker.data.local.dao.RoutineDao
import com.example.exercisetracker.data.mapper.toDomain
import com.example.exercisetracker.data.mapper.toEntity
import com.example.exercisetracker.domain.model.Routine
import com.example.exercisetracker.domain.repository.IRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoutineRepository(
    private val routineDao: RoutineDao
) : IRoutineRepository {

    override fun getAllRoutines(): Flow<List<Routine>> =
        routineDao.getAllRoutines().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(routine: Routine): Long =
        routineDao.insertRoutine(routine.toEntity())

    override suspend fun update(routine: Routine) =
        routineDao.updateRoutine(routine.toEntity())

    override suspend fun delete(id: Int) =
        routineDao.deleteRoutineById(id)
}
