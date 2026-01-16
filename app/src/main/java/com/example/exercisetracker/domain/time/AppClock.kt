package com.example.exercisetracker.domain.time

interface AppClock {
    fun now(): Long
}