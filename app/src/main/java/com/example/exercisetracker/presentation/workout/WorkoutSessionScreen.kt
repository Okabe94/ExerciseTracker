@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.example.exercisetracker.presentation.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes.Companion.Pill
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.exercisetracker.R
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme
import java.util.Locale
import java.util.Locale.getDefault

@Composable
fun WorkoutSessionRoot(
    backStack: NavBackStack<NavKey>,
    viewModel: WorkoutSessionViewModel
) {
    val state by viewModel.state.collectAsState()
    WorkoutSessionScreen(
        state = state,
        onAction = {
            viewModel.onAction(it)

            when (it) {
                WorkoutSessionAction.OnFinishWorkout -> backStack.removeAt(backStack.lastIndex)
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

@Preview
@Composable
private fun A() {
    ExerciseTrackerTheme {
        Surface {
            ExerciseSessionCard(
                "Hola",
                true,
                {},
                {},
                { _, _, _ -> },
                { _ -> },
                listOf()
            )
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
                        text = exerciseName.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                getDefault()
                            ) else it.toString()
                        },
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
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.weight),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = stringResource(R.string.reps),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = "",
                                    modifier = Modifier.width(48.dp)
                                )
                            }
                        }

                        sets.forEach { set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = set.weight,
                                    onValueChange = { onUpdateSet(set.id, it, set.reps) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = set.reps,
                                    onValueChange = { onUpdateSet(set.id, set.weight, it) },
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
                        .background(color = MaterialTheme.colorScheme.onSecondaryContainer)
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
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }

    }
}