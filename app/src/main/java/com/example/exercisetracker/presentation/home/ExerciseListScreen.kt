@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.example.exercisetracker.presentation.home

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.example.exercisetracker.R
import com.example.exercisetracker.presentation.navigation.Navigator
import com.example.exercisetracker.presentation.navigation.Route
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.example.exercisetracker.ui.theme.Gold400
import com.example.exercisetracker.ui.theme.Gold500
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ExerciseListRoot(
    navigator: Navigator,
    viewModel: ExerciseListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    ExerciseListScreen(
        state = state,
        onAction = {
            viewModel.onAction(it)
            when (it) {
                is ExerciseListAction.OnStartWorkout,
                is ExerciseListAction.OnResumeWorkout -> navigator.navigate(Route.Workout)

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
    Scaffold(
        modifier = Modifier.padding(16.dp),
        topBar = {
            WorkoutWeekCalendar(
                workoutDays = state.workoutDaysDone,
                currentDay = state.currentDay
            )
        },
        bottomBar = { ActionsSection(onAction = onAction, state = state) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 16.dp)
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

            if (state.confirmDialogVisible) {
                InformativeDialog(
                    title = stringResource(R.string.new_session_confirm_dialog_title),
                    message = stringResource(R.string.new_session_confirm_dialog_message),
                    onDismiss = { onAction(ExerciseListAction.OnShowConfirmDialog(false)) },
                    onConfirm = { onAction(ExerciseListAction.OnStartWorkout) }
                )
            }

            val adaptiveInfo = currentWindowAdaptiveInfo()
            if (adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MuscleSection(
                        modifier = Modifier.weight(1f),
                        onAction = onAction,
                        state = state
                    )

                    VerticalDivider(modifier = Modifier.fillMaxHeight())

                    ExerciseSection(
                        modifier = Modifier.weight(1f),
                        onAction = onAction,
                        state = state
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MuscleSection(
                        modifier = Modifier.weight(1f),
                        onAction = onAction,
                        state = state
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ExerciseSection(
                        modifier = Modifier.weight(1f),
                        onAction = onAction,
                        state = state
                    )
                }
            }
        }

    }
}

@Composable
private fun WorkoutWeekCalendar(
    workoutDays: Set<Int>,
    currentDay: Int
) {
    val days = listOf("L", "M", "X", "J", "V", "S", "D")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.activity_this_week),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEachIndexed { index, day ->
                val dayNumber = index + 1
                val isWorkoutDone = workoutDays.contains(dayNumber)
                val isToday = dayNumber == currentDay
                val isWeekDone = workoutDays.size >= 4

                DayNode(
                    dayName = day,
                    isWorkoutDone = isWorkoutDone,
                    isToday = isToday,
                    isWeekDone = isWeekDone
                )
            }
        }
    }
}

@Composable
private fun DayNode(
    dayName: String,
    isWorkoutDone: Boolean,
    isToday: Boolean,
    isWeekDone: Boolean
) {
    val node = getDayNodeColors(isWorkoutDone, isToday, isWeekDone)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color = node.backgroundColor)
                .border(
                    width = node.borderSize,
                    color = node.borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isWorkoutDone) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_local_fire_department_24),
                    contentDescription = null,
                    tint = node.tintColor,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = node.textColor
                )
            }
        }
    }
}

@Composable
private fun getDayNodeColors(
    isWorkoutDone: Boolean,
    isToday: Boolean,
    isWeekDone: Boolean
): DayNodeColors {
    val backgroundColor = when {
        isWeekDone && isWorkoutDone -> Gold500
        isWorkoutDone -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    }

    val borderColor = when {
        isWeekDone && isWorkoutDone -> Gold400
        isWorkoutDone -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.error
        else -> Color.Transparent
    }

    val tintColor = when {
        isWeekDone && isWorkoutDone -> Color.Black
        else -> MaterialTheme.colorScheme.onPrimary
    }

    val textColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderSize = when {
        isToday || isWeekDone -> 2.dp
        else -> 0.dp
    }

    return DayNodeColors(
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        tintColor = tintColor,
        textColor = textColor,
        borderSize = borderSize
    )
}

private data class DayNodeColors(
    val backgroundColor: Color,
    val borderColor: Color,
    val tintColor: Color,
    val textColor: Color,
    val borderSize: Dp
)


@Composable
private fun MuscleSection(
    modifier: Modifier = Modifier,
    onAction: (ExerciseListAction) -> Unit,
    state: ExerciseListState
) {
    Section(
        modifier = modifier,
        key = { it.id },
        isEmpty = state.muscleList.isEmpty(),
        itemList = state.muscleList,
        emptySection = {
            EmptySection(text = stringResource(R.string.no_muscles_found))
        },
        titleSection = {
            Text(
                text = stringResource(R.string.muscles),
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                modifier = Modifier.padding(0.dp),
                contentPadding = PaddingValues(0.dp),
                onClick = { onAction(ExerciseListAction.OnShowExerciseDialog(true)) }
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(R.string.add_muscle)
                )
            }
        }
    ) {
        SelectableAnimatedItem(
            name = it.name,
            isSelected = state.selectedMuscleIds.contains(it.id),
            onSelect = { onAction(ExerciseListAction.OnMuscleSelected(it.id)) }
        )
    }
}

@Composable
private fun ExerciseSection(
    modifier: Modifier = Modifier,
    onAction: (ExerciseListAction) -> Unit,
    state: ExerciseListState
) {
    Section(
        modifier = modifier,
        key = { it.id },
        isEmpty = state.exerciseList.isEmpty(),
        itemList = state.exerciseList,
        emptySection = {
            EmptySection(text = stringResource(R.string.no_exercises_found))
        },
        titleSection = {
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
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(R.string.add_exercise)
                    )
                }
            }
        }
    ) {
        SelectableAnimatedItem(
            name = it.name,
            isSelected = state.selectedExerciseIds.contains(it.id),
            onSelect = { onAction(ExerciseListAction.OnExerciseSelected(it.id)) }
        )
    }
}

@Composable
private fun <T> Section(
    modifier: Modifier = Modifier,
    isEmpty: Boolean,
    titleSection: @Composable () -> Unit,
    emptySection: @Composable ColumnScope.() -> Unit,
    itemList: List<T>,
    key: (T) -> Any,
    itemContent: @Composable (T) -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            titleSection()
        }

        Spacer(Modifier.height(8.dp))

        if (isEmpty) {
            emptySection()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = itemList,
                    key = { key(it) }
                ) { data -> itemContent(data) }
            }
        }
    }
}

@Composable
private fun ActionsSection(
    modifier: Modifier = Modifier,
    onAction: (ExerciseListAction) -> Unit,
    state: ExerciseListState
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        if (state.hasActiveWorkout && state.selectedExerciseIds.isEmpty()) {
            PrimaryButtonLayout(
                onClick = { onAction(ExerciseListAction.OnResumeWorkout) }
            ) { ResumeWorkoutButton() }
        } else if (state.hasActiveWorkout) {
            SecondaryButtonLayout(
                onClick = { onAction(ExerciseListAction.OnShowConfirmDialog(true)) }
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
private fun ColumnScope.EmptySection(modifier: Modifier = Modifier, text: String) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
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
private fun InformativeDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLargeEmphasized
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMediumEmphasized,
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.start))
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

@Preview(uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DayNodesLightPreview() {
    ExerciseTrackerTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Regular day
                Text(
                    text = "RD",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = false, isToday = false, isWeekDone = false)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Today
                Text(
                    text = "T",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = false, isToday = true, isWeekDone = false)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Workout done this day
                Text(
                    text = "WD TT",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = true, isToday = false, isWeekDone = false)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Workout done today
                Text(
                    text = "WD T",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = true, isToday = true, isWeekDone = false)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Today
                Text(
                    text = "T",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = false, isToday = true, isWeekDone = true)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Workout week completed regular day
                Text(
                    text = "WC RD",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = false, isToday = false, isWeekDone = true)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Workout week completed and done this day
                Text(
                    text = "WC TT",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = true, isToday = false, isWeekDone = true)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Workout week completed and done today
                Text(
                    text = "WC T",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = true, isToday = true, isWeekDone = true)
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DayNodesPreview() {
    ExerciseTrackerTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Regular day
                Text(
                    text = "RD",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = false, isToday = false, isWeekDone = false)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Today
                Text(
                    text = "T",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = false, isToday = true, isWeekDone = false)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Workout done this day
                Text(
                    text = "WD TT",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = true, isToday = false, isWeekDone = false)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Workout done today
                Text(
                    text = "WD T",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = true, isToday = true, isWeekDone = false)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Today
                Text(
                    text = "T",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = false, isToday = true, isWeekDone = true)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Workout week completed regular day
                Text(
                    text = "WC RD",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = false, isToday = false, isWeekDone = true)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Workout week completed and done this day
                Text(
                    text = "WC TT",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = true, isToday = false, isWeekDone = true)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Workout week completed and done today
                Text(
                    text = "WC T",
                    style = MaterialTheme.typography.labelSmall
                )
                DayNode("L", isWorkoutDone = true, isToday = true, isWeekDone = true)
            }
        }
    }
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