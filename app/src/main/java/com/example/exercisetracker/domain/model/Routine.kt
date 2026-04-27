package com.example.exercisetracker.domain.model

data class Routine(
    val id: Int = 0,
    val name: String,
    val exerciseIds: List<Int>
)
