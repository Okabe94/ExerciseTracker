@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.example.exercisetracker.presentation.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.exercisetracker.R
import com.example.exercisetracker.core.presentation.util.cap
import com.example.exercisetracker.presentation.navigation.Navigator
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme
import java.util.Locale.getDefault

@Composable
fun WorkoutSessionRoot(
    navigator: Navigator,
    viewModel: WorkoutSessionViewModel
) {
    val state by viewModel.state.collectAsState()
    WorkoutSessionScreen(
        state = state,
        onAction = {
            viewModel.onAction(it)

            when (it) {
                WorkoutSessionAction.OnFinishWorkout -> navigator.goBack()
                else -> Unit
            }
        },
    )
}

@Composable
fun WorkoutSessionScreen(
    onAction: (WorkoutSessionAction) -> Unit,
    state: WorkoutSessionState
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shapes = ButtonShapes(
                    shape = MaterialTheme.shapes.medium,
                    pressedShape = MaterialTheme.shapes.extraLarge
                ),
                onClick = { onAction(WorkoutSessionAction.OnFinishWorkout) },
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = stringResource(R.string.finish)
                )
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(
                items = state.listOfExercises,
                key = { it.id }
            ) { exercise ->

                ExerciseSessionCard(
                    exerciseName = exercise.name,
                    sets = exercise.sets,
                    expanded = exercise.expanded,
                    onAddSet = {
                        onAction(WorkoutSessionAction.OnAddSet(exercise.id))
                    },
                    onUpdateSet = { setId, weight, reps ->
                        onAction(
                            WorkoutSessionAction.OnUpdateSet(
                                exerciseId = exercise.id,
                                setId = setId,
                                weight = weight,
                                reps = reps
                            )
                        )
                    },
                    onToggleExpanded = {
                        onAction(WorkoutSessionAction.OnExpandList(exercise.id))
                    },
                    onRemoveSet = { setId ->
                        onAction(
                            WorkoutSessionAction.OnRemoveSet(
                                exerciseId = exercise.id,
                                setId = setId
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ExerciseSessionCard(
    exerciseName: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onAddSet: () -> Unit,
    onUpdateSet: (Int, String, String) -> Unit,
    onRemoveSet: (Int) -> Unit,
    sets: List<WorkoutSessionSet>,
) {
    val expandedRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        label = "Rotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exerciseName.cap(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = onAddSet) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.outline_add_24),
                            contentDescription = "Add Set"
                        )
                    }
                }

                AnimatedVisibility(expanded) {
                    Column {
                        if (sets.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(modifier = Modifier.width(32.dp)) // For set number alignment
                                Text(
                                    text = stringResource(R.string.weight),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.reps),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(48.dp)) // For delete button alignment
                            }
                        }

                        sets.forEachIndexed { index, set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Set Number Indicator
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = set.weight,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() }) {
                                            onUpdateSet(set.id, newValue, set.reps)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = set.reps,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() }) {
                                            onUpdateSet(set.id, set.weight, newValue)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                IconButton(onClick = { onRemoveSet(set.id) }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.outline_delete_24),
                                        contentDescription = "Remove Set"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (sets.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.secondaryContainer)
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 2.dp)
                        .clickable { onToggleExpanded() },
                ) {
                    Icon(
                        modifier = Modifier
                            .size(30.dp)
                            .rotate(expandedRotation)
                            .align(Alignment.Center),
                        imageVector = ImageVector.vectorResource(R.drawable.outline_arrow_drop_up_24),
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

    }
}

@Preview
@Composable
private fun ExerciseSessionCardPreview() {
    ExerciseTrackerTheme {
        Surface {
            ExerciseSessionCard(
                exerciseName = "Bicep Curls",
                expanded = true,
                onToggleExpanded = {},
                onAddSet = {},
                onUpdateSet = { _, _, _ -> },
                onRemoveSet = { _ -> },
                sets = listOf(
                    WorkoutSessionSet(id = 1, number = 1, weight = "20", reps = "12"),
                    WorkoutSessionSet(id = 2, number = 2, weight = "22", reps = "10")
                )
            )
        }
    }
}
