@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.example.exercisetracker.presentation.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.exercisetracker.R
import com.example.exercisetracker.presentation.navigation.Route
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ExerciseListRoot(
    backStack: NavBackStack<NavKey>,
    viewModel: ExerciseListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    ExerciseListScreen(
        state = state,
        onAction = {
            viewModel.onAction(it)
            when (it) {
                is ExerciseListAction.OnStartWorkout,
                is ExerciseListAction.OnResumeWorkout -> backStack.add(Route.Workout)

                else -> Unit
            }
        }
    )
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

        Text(
            text = stringResource(R.string.muscles),
            style = MaterialTheme.typography.titleLargeEmphasized,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.width(8.dp))

            AddMuscleButton(
                onClick = { onAction(ExerciseListAction.OnShowMuscleDialog(true)) }
            )

        }

        if (state.filteredMuscles.isEmpty()) {
            EmptyMuscles()
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = state.filteredMuscles,
                    key = { it.id }
                ) { muscle ->
                    SelectableAnimatedItem(
                        name = muscle.name,
                        isSelected = state.selectedMuscleIds.contains(muscle.id),
                        onSelect = { onAction(ExerciseListAction.OnMuscleSelected(muscle.id)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.exercises),
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontWeight = FontWeight.Bold
            )

            if (state.selectedMuscleIds.size == 1) {
                TextButton(
                    modifier = Modifier.padding(0.dp),
                    contentPadding = PaddingValues(0.dp),
                    onClick = { onAction(ExerciseListAction.OnShowExerciseDialog(true)) }
                ) {
                    Text(stringResource(R.string.add_exercise))
                }
            }
        }

        if (state.filteredExercises.isEmpty()) {
            EmptyExercises()
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = state.filteredExercises,
                    key = { it.id }
                ) { exercise ->
                    SelectableAnimatedItem(
                        name = exercise.name,
                        isSelected = state.selectedExerciseIds.contains(exercise.id),
                        onSelect = { onAction(ExerciseListAction.OnExerciseSelected(exercise.id)) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            if (state.hasActiveWorkout && state.selectedExerciseIds.isEmpty()) {
                PrimaryButtonLayout(
                    onClick = { onAction(ExerciseListAction.OnResumeWorkout) }
                ) { ResumeWorkoutButton() }
            } else if (state.hasActiveWorkout) {
                SecondaryButtonLayout(
                    onClick = { onAction(ExerciseListAction.OnStartWorkout) }
                ) { StartWorkoutButton() }
                PrimaryButtonLayout(
                    onClick = { onAction(ExerciseListAction.OnResumeWorkout) }
                ) { ResumeWorkoutButton() }
            } else if (state.selectedExerciseIds.isNotEmpty()) {
                PrimaryButtonLayout(
                    onClick = { onAction(ExerciseListAction.OnStartWorkout) }
                ) { StartWorkoutButton() }
            }
        }
    }
}

@Composable
private fun RowScope.PrimaryButtonLayout(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    FilledTonalButton(
        modifier = modifier.weight(1f),
        shapes = ButtonShapes(
            shape = MaterialTheme.shapes.medium,
            pressedShape = MaterialTheme.shapes.extraLarge
        ),
        onClick = onClick,
        content = content
    )
}

@Composable
private fun RowScope.SecondaryButtonLayout(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        modifier = modifier.weight(1f),
        shapes = ButtonShapes(
            shape = MaterialTheme.shapes.medium,
            pressedShape = MaterialTheme.shapes.extraLarge
        ),
        onClick = onClick,
        content = content
    )
}

@Composable
private fun ResumeWorkoutButton() {
    CircularProgressIndicator(modifier = Modifier.size(24.dp))
    Spacer(Modifier.width(8.dp))
    Text(
        modifier = Modifier.padding(vertical = 8.dp),
        text = stringResource(id = R.string.resume)
    )
}

@Composable
private fun StartWorkoutButton() {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.outline_exercise_24),
        contentDescription = null
    )
    Spacer(Modifier.width(8.dp))
    Text(
        modifier = Modifier.padding(vertical = 8.dp),
        text = stringResource(id = R.string.workout)
    )
}

@Composable
fun EmptyMuscles(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_muscles_found),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColumnScope.EmptyExercises(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_exercises_found),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SelectableAnimatedItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onSelect: () -> Unit,
    name: String,
) {
    val checkAnimation = remember { Animatable(0f) }
    val unCheckAnimation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .graphicsLayer {
                rotationZ = checkAnimation.value
                translationY = unCheckAnimation.value
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onClick = {
            scope.launch {
                onSelect()
                if (!isSelected) animateCheck(checkAnimation)
                else animateUncheck(unCheckAnimation)
            }
        }
    ) {

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = name,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMediumEmphasized,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun AddNameDialog(
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

@Composable
private fun AddMuscleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val iSource = remember { MutableInteractionSource() }
    val isPressed by iSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "pressAnimation"
    )

    OutlinedIconButton(
        modifier = modifier
            .size(width = 50.dp, height = 50.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = MaterialTheme.shapes.medium,
        interactionSource = iSource,
        onClick = onClick,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.outline_add_24),
            contentDescription = "add"
        )
    }
}

private suspend fun animateUncheck(anim: Animatable<Float, AnimationVector1D>) {
    anim.animateTo(
        targetValue = 15f,
        animationSpec = tween(100)
    )
    anim.animateTo(
        targetValue = 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy)
    )
}

private suspend fun animateCheck(anim: Animatable<Float, AnimationVector1D>) {
    anim.animateTo(
        targetValue = 2f,
        animationSpec = tween(100)
    )
    anim.animateTo(
        targetValue = -2f,
        animationSpec = tween(100)
    )
    anim.animateTo(
        targetValue = 0f,
        animationSpec = tween(100)
    )
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ExerciseListScreenPreview() {
    ExerciseTrackerTheme {
        Surface {
            ExerciseListScreen(
                onAction = {},
                state = ExerciseListState(
                    hasActiveWorkout = false,
                    selectedExerciseIds = listOf(1)
                )
            )
        }
    }
}