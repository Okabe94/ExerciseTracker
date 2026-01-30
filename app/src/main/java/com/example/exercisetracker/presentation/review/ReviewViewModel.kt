package com.example.exercisetracker.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class ReviewViewModel(
    private val day: Int
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ReviewState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                /** Load initial data here **/
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ReviewState()
        )

    fun onAction(action: ReviewAction) {
        when (action) {
            else -> TODO("Handle actions")
        }
    }
}
