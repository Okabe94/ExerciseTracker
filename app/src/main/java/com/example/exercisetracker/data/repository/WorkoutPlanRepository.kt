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
            clock.getMillisAtStartOfDay(1)
        ).map { list ->
            list.map {
                it.toDomain(clock.getDayNumberFromMillis(it.date))
            }
        }

    /**
     * Añadir funcionalidad de review (repetir entreno) días pasado
     * Añadir funcionalidad de actualización de agendado
     * Añadir flujo de borrado de entreno agendado
     * Eliminar entrenos pasados (misma semana)
     * Cambiar librería de gráficas
     * Eliminar sets desde gráfica
     *
     * Añadir broadcast receiver del día y arreglar flows para que lo usen
     * Mover el día seleccionado al estado de la vista
     * Cambiar indicador de día seleccionado
     * Cambiar filtros de músculos
     * Cambiar flujos de añadido de ejercicios (siempre visible)
     */

    override suspend fun insertWorkoutPlan(workoutPlan: WorkoutPlan): Long =
        workoutPlanDao.insertWorkoutPlan(
            workoutPlan.toEntity(
                millis = clock.getMillisForDay(workoutPlan.day - clock.getCurrentNumberDay())
            )
        )

    override suspend fun updateWorkoutPlan(workoutPlan: WorkoutPlan) {
        workoutPlanDao.updateWorkoutPlan(
            workoutPlan.toEntity(
                millis = clock.getMillisForDay(workoutPlan.day - clock.getCurrentNumberDay())
            )
        )
    }

    override suspend fun deleteWorkoutPlan(workoutPlan: WorkoutPlan) =
        workoutPlanDao.deleteWorkoutPlan(
            workoutPlan.toEntity(
                millis = clock.getMillisForDay(workoutPlan.day - clock.getCurrentNumberDay())
            )
        )
}
