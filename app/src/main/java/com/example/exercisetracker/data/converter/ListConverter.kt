package com.example.exercisetracker.data.converter

import androidx.room.TypeConverter

class IntListConverter {
    @TypeConverter
    fun fromIntList(value: List<Int>): String = value.joinToString(",")

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        if (value.isBlank()) return emptyList()
        return value.split(',').map { it.toInt() }
    }
}
