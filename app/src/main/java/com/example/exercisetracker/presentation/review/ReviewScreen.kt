package com.example.exercisetracker.presentation.review

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme

@Composable
fun ReviewRoot(
    viewModel: ReviewViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ReviewScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ReviewScreen(
    state: ReviewState,
    onAction: (ReviewAction) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("HOLAAAA", modifier = Modifier.align(Alignment.Center))
    }
}

@Preview
@Composable
private fun Preview() {
    ExerciseTrackerTheme {
        ReviewScreen(
            state = ReviewState(),
            onAction = {}
        )
    }
}
