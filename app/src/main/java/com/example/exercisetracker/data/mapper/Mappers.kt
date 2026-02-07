package com.example.exercisetracker.data.mapper

import com.example.exercisetracker.data.local.entity.ExerciseEntity
import com.example.exercisetracker.data.local.entity.MuscleEntity
import com.example.exercisetracker.data.local.entity.WorkoutPlanEntity
import com.example.exercisetracker.data.local.entity.WorkoutSessionEntity
import com.example.exercisetracker.data.local.entity.WorkoutSetEntity
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.model.WorkoutPlan
import com.example.exercisetracker.domain.model.WorkoutSession
import com.example.exercisetracker.domain.model.WorkoutSet

fun MuscleEntity.toDomain() = Muscle(
    id = id,
    name = name
)

fun ExerciseEntity.toDomain() = Exercise(
    id = id,
    name = name,
    targetMuscleId = targetMuscleId
)

fun WorkoutSessionEntity.toDomain() = WorkoutSession(
    id = id,
    startTime = startTime,
    endTime = endTime,
    exercises = exercises,
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

fun WorkoutPlanEntity.toDomain(day: Int) = WorkoutPlan(
    id = id,
    day = day,
    exercises = exercises
)

fun Muscle.toEntity() = MuscleEntity(
    id = id,
    name = name
)

fun Exercise.toEntity() = ExerciseEntity(
    id = id,
    name = name,
    targetMuscleId = targetMuscleId
)


fun WorkoutSession.toEntity() = WorkoutSessionEntity(
    startTime = startTime,
    endTime = endTime,
    exercises = exercises,
    isCompleted = isCompleted
)

fun WorkoutSet.toEntity() = WorkoutSetEntity(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    setNumber = setNumber,
    weight = weight,
    reps = reps
)

fun WorkoutPlan.toEntity(millis: Long) = WorkoutPlanEntity(
    id = id,
    date = millis,
    exercises = exercises
)

