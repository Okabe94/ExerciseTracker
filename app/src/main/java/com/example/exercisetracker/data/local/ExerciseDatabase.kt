package com.example.exercisetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.exercisetracker.data.converter.IntListConverter
import com.example.exercisetracker.data.local.dao.ExerciseDao
import com.example.exercisetracker.data.local.dao.MuscleDao
import com.example.exercisetracker.data.local.dao.WorkoutDao
import com.example.exercisetracker.data.local.entity.ExerciseEntity
import com.example.exercisetracker.data.local.entity.MuscleEntity
import com.example.exercisetracker.data.local.entity.WorkoutSessionEntity
import com.example.exercisetracker.data.local.entity.WorkoutSetEntity

@Database(
    entities = [
        ExerciseEntity::class,
        MuscleEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSetEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(IntListConverter::class)
abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun muscleDao(): MuscleDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var Instance: ExerciseDatabase? = null

        fun getDatabase(context: Context): ExerciseDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context = context,
                    klass = ExerciseDatabase::class.java,
                    name = "exercise_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(ExerciseDatabaseCallback())
                    .build()
                    .also { Instance = it }
            }
        }
    }

    private class ExerciseDatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            populateMuscles(db)
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            populateMuscles(db)
        }

        private fun populateMuscles(db: SupportSQLiteDatabase) {
            val cursor = db.query("SELECT count(*) FROM muscles")
            if (cursor.moveToFirst()) {
                val count = cursor.getInt(0)
                val muscles = listOf(
                    "Pecho", "Espalda", "Hombros", "Bíceps", "Triceps",
                    "Cuádriceps", "Isquiotibiales", "Gluteos", "Pantorrillas", "Abdominales",
                    "Antebrazos", "Trapecio", "Dorsal", "Abductores", "Adductores"
                )
                if (count < muscles.size) {
                    muscles.forEach { muscle ->
                        db.execSQL("INSERT OR IGNORE INTO muscles (name) VALUES ('$muscle')")
                    }
                }
            }
            cursor.close()
        }
    }
}
