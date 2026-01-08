package com.example.exercisetracker.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.exercisetracker.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun ExerciseListRoot(
    backStack: NavBackStack<NavKey>,
    viewModel: ExerciseListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    ExerciseListScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun ExerciseListScreen(
    onAction: (ExerciseListAction) -> Unit,
    state: ExerciseListState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.muscles),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                shape = MaterialTheme.shapes.medium,
                onValueChange = { onAction(ExerciseListAction.OnSearchQueryChange(it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.search)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.width(4.dp))

            FilledTonalIconButton(
                onClick = { onAction(ExerciseListAction.OnShowMuscleDialog(true)) },
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_add_24),
                    contentDescription = "add"
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = state.filteredMuscles,
                key = { it.id }
            ) { muscle ->
                SelectableItem(
                    text = muscle.name,
                    isSelected = state.selectedMuscleIds.contains(muscle.id),
                    onClick = { onAction(ExerciseListAction.OnMuscleSelected(muscle.id)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.exercise),
                style = MaterialTheme.typography.titleMedium
            )

            if (state.selectedMuscleIds.size == 1) {
                TextButton(
                    onClick = { onAction(ExerciseListAction.OnShowExerciseDialog(true)) }
                ) {
                    Text(stringResource(R.string.add_exercise))
                }
            }
        }

        if (state.filteredExercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.no_exercises_found),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.filteredExercises, key = { it.id }) { exercise ->
                    SelectableItem(
                        text = exercise.name,
                        isSelected = state.selectedExerciseIds.contains(exercise.id),
                        onClick = {
                            onAction(ExerciseListAction.OnExerciseSelected(exercise.id))
                        }
                    )
                }
            }
        }
    }

    if (state.addExerciseDialogVisible) {
        AddNameDialog(
            title = stringResource(R.string.add_exercise),
            label = stringResource(R.string.exercise_name),
            onDismiss = { onAction(ExerciseListAction.OnShowExerciseDialog(false)) },
            onConfirm = { name -> onAction(ExerciseListAction.OnAddExercise(name)) }
        )
    }

    if (state.addMuscleDialogVisible) {
        AddNameDialog(
            title = stringResource(R.string.add_muscle),
            label = stringResource(R.string.muscle_name),
            onDismiss = { onAction(ExerciseListAction.OnShowMuscleDialog(false)) },
            onConfirm = { name -> onAction(ExerciseListAction.OnAddMuscle(name)) }
        )
    }
}


@Composable
fun SelectableItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
            if (isSelected) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_check_24),
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AddNameDialog(
    title: String,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text)
                    }
                }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.Cancel))
            }
        }
    )
}
