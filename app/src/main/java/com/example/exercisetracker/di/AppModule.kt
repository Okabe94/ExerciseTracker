package com.example.exercisetracker.di

import com.example.exercisetracker.data.local.ExerciseDatabase
import com.example.exercisetracker.data.repository.ExerciseRepository
import com.example.exercisetracker.data.repository.MuscleRepository
import com.example.exercisetracker.data.repository.WorkoutPlanRepository
import com.example.exercisetracker.data.repository.WorkoutRepository
import com.example.exercisetracker.data.time.ExerciseClock
import com.example.exercisetracker.data.timezone.ExerciseTimeZone
import com.example.exercisetracker.domain.repository.IExerciseRepository
import com.example.exercisetracker.domain.repository.IMuscleRepository
import com.example.exercisetracker.domain.repository.IWorkoutPlanRepository
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import com.example.exercisetracker.domain.time.AppClock
import com.example.exercisetracker.domain.timezone.AppTimeZone
import com.example.exercisetracker.presentation.home.ExerciseListViewModel
import com.example.exercisetracker.presentation.metrics.MetricsViewModel
import com.example.exercisetracker.presentation.workout.WorkoutSessionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { ExerciseDatabase.getDatabase(androidContext()) }

    // Dao
    single { get<ExerciseDatabase>().exerciseDao() }
    single { get<ExerciseDatabase>().muscleDao() }
    single { get<ExerciseDatabase>().workoutDao() }
    single { get<ExerciseDatabase>().workoutPlanDao() }

    // Helpers
    singleOf(::ExerciseTimeZone) bind AppTimeZone::class
    singleOf(::ExerciseClock) bind AppClock::class

    // Repository
    singleOf(::ExerciseRepository) bind IExerciseRepository::class
    singleOf(::MuscleRepository) bind IMuscleRepository::class
    singleOf(::WorkoutRepository) bind IWorkoutRepository::class
    singleOf(::WorkoutPlanRepository) bind IWorkoutPlanRepository::class

    // ViewModel
    viewModelOf(::ExerciseListViewModel)
    viewModelOf(::WorkoutSessionViewModel)
    viewModelOf(::MetricsViewModel)
}
