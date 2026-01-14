package com.example.exercisetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "muscles")
data class MuscleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
