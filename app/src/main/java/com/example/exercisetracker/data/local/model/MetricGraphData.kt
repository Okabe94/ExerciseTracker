package com.example.exercisetracker.data.local.model

data class MetricGraphData(
    val id: Int,
    val sessionId: Int,
    val startTime: Long,
    val weight: Float,
    val reps: Int
)