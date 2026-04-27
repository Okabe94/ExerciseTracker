package com.example.exercisetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.exercisetracker.data.converter.IntListConverter
import com.example.exercisetracker.data.local.dao.ExerciseDao
import com.example.exercisetracker.data.local.dao.MuscleDao
import com.example.exercisetracker.data.local.dao.RoutineDao
import com.example.exercisetracker.data.local.dao.WorkoutDao
import com.example.exercisetracker.data.local.dao.WorkoutPlanDao
import com.example.exercisetracker.data.local.entity.ExerciseEntity
import com.example.exercisetracker.data.local.entity.MuscleEntity
import com.example.exercisetracker.data.local.entity.RoutineEntity
import com.example.exercisetracker.data.local.entity.WorkoutPlanEntity
import com.example.exercisetracker.data.local.entity.WorkoutSessionEntity
import com.example.exercisetracker.data.local.entity.WorkoutSetEntity

@Database(
    entities = [
        ExerciseEntity::class,
        MuscleEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSetEntity::class,
        WorkoutPlanEntity::class,
        RoutineEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(IntListConverter::class)
abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun muscleDao(): MuscleDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun routineDao(): RoutineDao

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
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                    .addCallback(ExerciseDatabaseCallback())
                    .build()
                    .also { Instance = it }
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `workout_plan` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` INTEGER NOT NULL, `exercises` TEXT NOT NULL)"
                )
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `routines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `exerciseIds` TEXT NOT NULL)"
                )
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
