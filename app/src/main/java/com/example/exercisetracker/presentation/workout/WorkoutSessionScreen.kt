package com.example.exercisetracker.presentation.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.exercisetracker.R
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun WorkoutSessionScreen(
    exerciseIds: Set<Int>,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkoutSessionViewModel = koinViewModel(parameters = { parametersOf(exerciseIds) })
) {
    val exercises by viewModel.exercises.collectAsState()
    val sets by viewModel.sets.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Button(
                onClick = {
                    viewModel.finishWorkout()
                    onFinish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Finish Workout")
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
            items(exercises, key = { it.id }) { exercise ->
                ExerciseSessionCard(
                    exerciseName = exercise.name,
                    sets = sets[exercise.id] ?: emptyList(),
                    onAddSet = { viewModel.addSet(exercise.id) },
                    onUpdateSet = { setId, weight, reps ->
                        viewModel.updateSet(exercise.id, setId, weight, reps)
                    },
                    onRemoveSet = { setId ->
                        viewModel.removeSet(exercise.id, setId)
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseSessionCard(
    exerciseName: String,
    sets: List<UiWorkoutSet>,
    onAddSet: () -> Unit,
    onUpdateSet: (String, String, String) -> Unit,
    onRemoveSet: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
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
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onAddSet) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.outline_add_24),
                        contentDescription = "Add Set"
                    )
                }
            }

            if (sets.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Weight",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Reps",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Spacer for delete button
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
