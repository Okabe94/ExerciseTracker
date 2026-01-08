package com.example.exercisetracker

import android.app.Application
import com.example.exercisetracker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ExerciseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ExerciseApplication)
            modules(appModule)
        }
    }
}