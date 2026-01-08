package com.example.exercisetracker.data.mapper

import com.example.exercisetracker.data.local.ExerciseEntity
import com.example.exercisetracker.data.local.MuscleEntity
import com.example.exercisetracker.data.local.WorkoutSessionEntity
import com.example.exercisetracker.data.local.WorkoutSetEntity
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.model.WorkoutSession
import com.example.exercisetracker.domain.model.WorkoutSet

fun MuscleEntity.toDomain() = Muscle(
    id = id,
    name = name
)

fun Muscle.toEntity() = MuscleEntity(
    id = id,
    name = name
)

fun ExerciseEntity.toDomain() = Exercise(
    id = id,
    name = name,
    targetMuscleId = targetMuscleId
)

fun Exercise.toEntity() = ExerciseEntity(
    id = id,
    name = name,
    targetMuscleId = targetMuscleId
)

fun WorkoutSessionEntity.toDomain() = WorkoutSession(
    id = id,
    startTime = startTime,
    endTime = endTime,
    isCompleted = isCompleted
)

fun WorkoutSession.toEntity() = WorkoutSessionEntity(
    id = id,
    startTime = startTime,
    endTime = endTime,
    isCompleted = isCompleted
)

fun WorkoutSetEntity.toDomain() = WorkoutSet(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    setNumber = setNumber,
    weight = weight,
    reps = reps
)

fun WorkoutSet.toEntity() = WorkoutSetEntity(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    setNumber = setNumber,
    weight = weight,
    reps = reps
)
