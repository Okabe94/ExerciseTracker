@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.exercisetracker.presentation.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.exercisetracker.R
import com.example.exercisetracker.core.presentation.util.cap
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.model.Routine
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme

@Composable
fun RoutinesBottomSheet(
    state: ExerciseListState,
    onAction: (ExerciseListAction) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { onAction(ExerciseListAction.OnCloseRoutinesSheet) },
        sheetState = sheetState
    ) {
        when (val content = state.routineSheetContent) {
            RoutineSheetContent.List -> RoutineListPage(
                state = state,
                onAction = onAction
            )
            is RoutineSheetContent.Editor -> RoutineEditorPage(
                routineId = content.routineId,
                state = state,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun RoutineListPage(
    state: ExerciseListState,
    onAction: (ExerciseListAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.routines),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                contentPadding = PaddingValues(horizontal = 8.dp),
                onClick = { onAction(ExerciseListAction.OnNavigateToRoutineEditor(null)) }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_add_24),
                    contentDescription = null
                )
                Spacer(Modifier.width(4.dp))
                Text(text = stringResource(R.string.new_routine))
            }
        }

        Spacer(Modifier.height(8.dp))

        if (state.routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_routines_found),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.routines, key = { it.id }) { routine ->
                    RoutineItem(
                        name = routine.name,
                        onApply = { onAction(ExerciseListAction.OnApplyRoutine(routine)) },
                        onEdit = { onAction(ExerciseListAction.OnNavigateToRoutineEditor(routine.id)) },
                        onDelete = { onAction(ExerciseListAction.OnDeleteRoutine(routine.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineItem(
    name: String,
    onApply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onApply
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_refresh_24),
                    contentDescription = null
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_delete_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RoutineEditorPage(
    routineId: Int?,
    state: ExerciseListState,
    onAction: (ExerciseListAction) -> Unit
) {
    val editor = state.routineEditorState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onAction(ExerciseListAction.OnNavigateBackToRoutineList) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_chevron_backward_24),
                    contentDescription = null
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = if (routineId != null) stringResource(R.string.edit_routine_title)
                       else stringResource(R.string.new_routine_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Button(
                enabled = editor.name.isNotBlank(),
                onClick = { onAction(ExerciseListAction.OnSaveRoutine) }
            ) {
                Text(text = stringResource(R.string.save_routine))
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = editor.name,
            onValueChange = { onAction(ExerciseListAction.OnRoutineNameChanged(it)) },
            label = { Text(stringResource(R.string.routine_name)) },
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.muscles),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.muscleList, key = { it.id }) { muscle ->
                FilterChip(
                    selected = editor.selectedMuscleIds.contains(muscle.id),
                    onClick = { onAction(ExerciseListAction.OnRoutineEditorMuscleSelected(muscle.id)) },
                    label = { Text(muscle.name.cap()) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.exercises),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(state.allExercises, key = { it.id }) { exercise ->
                SelectableAnimatedItem(
                    name = exercise.name,
                    isSelected = editor.selectedExerciseIds.contains(exercise.id),
                    onSelect = { onAction(ExerciseListAction.OnRoutineEditorExerciseSelected(exercise.id)) }
                )
            }
        }
    }
}

private val previewMuscles = listOf(
    Muscle(1, "Pecho"),
    Muscle(2, "Espalda"),
    Muscle(3, "Hombros"),
)

private val previewExercises = listOf(
    Exercise(1, "Press Banca", 1),
    Exercise(2, "Fondos", 1),
    Exercise(3, "Jalón Polea", 2),
    Exercise(4, "Remo", 2),
    Exercise(5, "Press Militar", 3),
)

private val previewRoutines = listOf(
    Routine(1, "Push Day A", listOf(1, 2)),
    Routine(2, "Pull Day", listOf(3, 4)),
    Routine(3, "Hombros", listOf(5)),
)

@Preview(showBackground = true)
@Composable
private fun RoutineListPagePreview() {
    ExerciseTrackerTheme {
        RoutineListPage(
            state = ExerciseListState(
                routines = previewRoutines,
                muscleList = previewMuscles,
                allExercises = previewExercises
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RoutineListEmptyPagePreview() {
    ExerciseTrackerTheme {
        RoutineListPage(
            state = ExerciseListState(
                routines = emptyList(),
                muscleList = previewMuscles,
                allExercises = previewExercises
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RoutineEditorNewPagePreview() {
    ExerciseTrackerTheme {
        RoutineEditorPage(
            routineId = null,
            state = ExerciseListState(
                muscleList = previewMuscles,
                allExercises = previewExercises,
                routineEditorState = RoutineEditorState(
                    name = "",
                    selectedMuscleIds = setOf(1),
                    selectedExerciseIds = setOf(1)
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RoutineEditorEditPagePreview() {
    ExerciseTrackerTheme {
        RoutineEditorPage(
            routineId = 1,
            state = ExerciseListState(
                muscleList = previewMuscles,
                allExercises = previewExercises,
                routineEditorState = RoutineEditorState(
                    name = "Push Day A",
                    selectedMuscleIds = setOf(1),
                    selectedExerciseIds = setOf(1, 2)
                )
            ),
            onAction = {}
        )
    }
}
