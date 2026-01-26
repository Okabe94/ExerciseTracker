package com.example.exercisetracker.di

import com.example.exercisetracker.data.local.ExerciseDatabase
import com.example.exercisetracker.data.repository.ExerciseRepository
import com.example.exercisetracker.data.repository.MuscleRepository
import com.example.exercisetracker.data.repository.WorkoutRepository
import com.example.exercisetracker.data.time.ExerciseClock
import com.example.exercisetracker.data.timezone.ExerciseTimeZone
import com.example.exercisetracker.domain.repository.IExerciseRepository
import com.example.exercisetracker.domain.repository.IMuscleRepository
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import com.example.exercisetracker.domain.time.AppClock
import com.example.exercisetracker.domain.timezone.AppTimeZone
import com.example.exercisetracker.presentation.home.ExerciseListViewModel
import com.example.exercisetracker.presentation.metrics.MetricsViewModel
import com.example.exercisetracker.presentation.workout.WorkoutSessionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        ExerciseDatabase.getDatabase(androidContext())
    }

    single { get<ExerciseDatabase>().exerciseDao() }
    single { get<ExerciseDatabase>().muscleDao() }
    single { get<ExerciseDatabase>().workoutDao() }

    single<AppTimeZone> { ExerciseTimeZone() }
    single<AppClock> { ExerciseClock(get()) }

    // Bind implementations to interfaces
    single<IExerciseRepository> { ExerciseRepository(get()) }
    single<IMuscleRepository> { MuscleRepository(get()) }
    single<IWorkoutRepository> { WorkoutRepository(get(), get()) }

    // ViewModel
    viewModel { ExerciseListViewModel(get(), get(), get()) }
    viewModel { WorkoutSessionViewModel(get(), get()) }
    viewModel { MetricsViewModel(get(), get(), get(), get()) }
}
