package com.example.exercisetracker.presentation.metrics

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MetricsViewModel : ViewModel() {

    private val _state = MutableStateFlow(MetricsState())
    val state = _state.asStateFlow()

    fun onAction(action: MetricsAction) {
        when (action) {
            else -> TODO("Handle actions")
        }
    }
}
