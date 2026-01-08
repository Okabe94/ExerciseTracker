package com.example.exercisetracker.di

import com.example.exercisetracker.data.local.ExerciseDatabase
import com.example.exercisetracker.data.repository.ExerciseRepository
import com.example.exercisetracker.data.repository.MuscleRepository
import com.example.exercisetracker.data.repository.WorkoutRepository
import com.example.exercisetracker.domain.repository.IExerciseRepository
import com.example.exercisetracker.domain.repository.IMuscleRepository
import com.example.exercisetracker.domain.repository.IWorkoutRepository
import com.example.exercisetracker.presentation.home.ExerciseListViewModel
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
    
    // Bind implementations to interfaces
    single<IExerciseRepository> { ExerciseRepository(get()) }
    single<IMuscleRepository> { MuscleRepository(get()) }
    single<IWorkoutRepository> { WorkoutRepository(get()) }
    
    // ViewModel
    viewModel { ExerciseListViewModel(get(), get(), get()) }
    viewModel { (exerciseIds: Set<Int>) -> WorkoutSessionViewModel(get(), get(), exerciseIds) }
}
