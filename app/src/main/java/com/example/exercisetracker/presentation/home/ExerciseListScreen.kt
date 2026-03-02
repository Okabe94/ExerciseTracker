@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.example.exercisetracker.presentation.home

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.example.exercisetracker.R
import com.example.exercisetracker.core.presentation.util.ObserveAsEvents
import com.example.exercisetracker.presentation.home.ExerciseListAction.*
import com.example.exercisetracker.presentation.navigation.Navigator
import com.example.exercisetracker.presentation.navigation.Route
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
        events = viewModel.events,
        onAction = {
            viewModel.onAction(it)
            when (it) {
                is OnStartWorkout,
                is OnResumeWorkout -> navigator.navigate(Route.Workout)

                is OnNavigateToReview -> navigator.navigate(Route.Review(it.day))
                else -> Unit
            }
        }
    )
}

@Composable
fun ExerciseListScreen(
    onAction: (ExerciseListAction) -> Unit,
    events: Flow<ExerciseListEvent>,
    state: ExerciseListState,
) {
    val snackState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ObserveAsEvents(events) { event ->
        handleEvents(onAction, event, snackState, context, scope)
    }

    Scaffold(
        modifier = Modifier.padding(16.dp),
        topBar = {
            WorkoutWeekCalendar(
                onDayNodeClick = { onAction(OnDayNodeSelected(it)) },
                workoutDays = state.workoutDaysDone,
                plannedDays = state.plannedWorkouts,
                currentDay = state.currentDay,
                daySelected = state.daySelected,
                mode = state.screenMode
            )
        },
        snackbarHost = {
            SnackbarHost(snackState) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        text = it.visuals.message,
                        style = MaterialTheme.typography.bodyMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
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
                    onDismiss = { onAction(OnShowExerciseDialog(false)) },
                    onConfirm = { name -> onAction(OnAddExercise(name)) }
                )
            }

            if (state.addMuscleDialogVisible) {
                AddNameDialog(
                    title = stringResource(R.string.add_muscle),
                    label = stringResource(R.string.muscle_name),
                    onDismiss = { onAction(OnShowMuscleDialog(false)) },
                    onConfirm = { name -> onAction(OnAddMuscle(name)) }
                )
            }

            if (state.activeWorkoutDialogVisible) {
                InformativeDialog(
                    title = stringResource(R.string.new_session_confirm_dialog_title),
                    message = stringResource(R.string.new_session_confirm_dialog_message),
                    onDismiss = { onAction(OnShowActiveWorkoutDialog(false)) },
                    onConfirm = { onAction(OnStartWorkout) }
                )
            }

            if (state.deletePlanDialogVisible) {
                DeleteConfirmationDialog(
                    title = stringResource(R.string.delete_workout_plan_dialog_title),
                    message = stringResource(R.string.delete_workout_plan_dialog_message),
                    onDismiss = { onAction(OnShowDeletePlannedDialog(false)) },
                    onConfirm = { onAction(OnDeletePlannedWorkout) }
                )
            }

            if (state.deleteDayWorkoutDialogVisible) {
                DeleteConfirmationDialog(
                    title = stringResource(R.string.delete_workout_dialog_title),
                    message = stringResource(R.string.delete_workout_dialog_message),
                    onDismiss = { onAction(OnShowDeleteWorkoutDialog(false)) },
                    onConfirm = { onAction(OnDeleteDayWorkout) }
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

fun handleEvents(
    onAction: (ExerciseListAction) -> Unit,
    event: ExerciseListEvent,
    snackState: SnackbarHostState,
    context: Context,
    scope: CoroutineScope
) {
    if (event is ExerciseListEvent.SendToReview) {
        onAction(OnNavigateToReview(event.day))
    }

    val message = when (event) {
        is ExerciseListEvent.ErrorMessage -> event.reason.asString(context)
        is ExerciseListEvent.WorkoutSaved -> context.getString(R.string.saved_workout_message)
        is ExerciseListEvent.WorkoutUpdated -> context.getString(R.string.updated_workout_message)
        ExerciseListEvent.WorkoutDeleted -> context.getString(R.string.deleted_workout_message)
        else -> context.getString(R.string.generic_error)
    }

    showSnackBarMessage(
        scope = scope,
        hostState = snackState,
        message = message
    )
}

@Composable
private fun WorkoutWeekCalendar(
    onDayNodeClick: (Int) -> Unit,
    workoutDays: Set<Int>,
    plannedDays: Set<Int>,
    currentDay: Int,
    daySelected: Int,
    mode: ScreenMode
) {
    val days = listOf("L", "M", "X", "J", "V", "S", "D")

    val title = when (mode) {
        is ScreenMode.Planning -> R.string.planning
        ScreenMode.Workout -> R.string.activity_this_week
        ScreenMode.Review -> R.string.activity_this_week
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Box {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val itemWidth = maxWidth / days.size

            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEachIndexed { index, day ->
                    val dayNumber = index + 1
                    val isWorkoutDone = workoutDays.contains(dayNumber)
                    val isToday = dayNumber == currentDay
                    val isPlanned = plannedDays.contains(dayNumber) && dayNumber > currentDay
                    val isWeekDone = workoutDays.size >= 4

                    val node = getDayNode(
                        text = day,
                        isWorkoutDone = isWorkoutDone,
                        isToday = isToday,
                        isWeekDone = isWeekDone,
                        isPlanned = isPlanned,
                        isPlanning = mode is ScreenMode.Planning && mode.day == dayNumber
                    )

                    Box(
                        modifier = Modifier.width(itemWidth),
                        contentAlignment = Alignment.Center
                    ) {
                        DayNode(
                            onClick = { onDayNodeClick(dayNumber) },
                            node = node
                        )
                    }
                }
            }

            val indicatorWidth = 20.dp
            val indicatorOffset =
                (itemWidth * (daySelected - 1)) + (itemWidth / 2) - (indicatorWidth / 2)

            val animatedIndicatorOffset by animateDpAsState(
                targetValue = indicatorOffset,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "indicator_offset"
            )

            Box(
                modifier = Modifier
                    .offset(x = animatedIndicatorOffset)
                    .padding(top = 55.dp)
                    .width(indicatorWidth)
                    .height(2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

@Composable
private fun DayNode(
    onClick: () -> Unit,
    node: BasicDayNode
) {
    Column(
        modifier = node
            .containerModifier()
            .then(Modifier.padding(4.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color = node.backgroundColor())
                .clickable { onClick() }
                .border(
                    width = node.borderSize,
                    color = node.borderColor(),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center,
            content = { node.GetNode() }
        )
    }
}

private fun getDayNode(
    text: String,
    isWorkoutDone: Boolean,
    isToday: Boolean,
    isWeekDone: Boolean,
    isPlanned: Boolean,
    isPlanning: Boolean
): BasicDayNode = when {
    isPlanned -> PlannedWorkoutDayNode
    isPlanning -> PlanningWorkoutDayNode
    isWeekDone && isWorkoutDone -> WeekAndWorkoutDayNode
    isWorkoutDone -> WorkoutDayNode
    isToday -> TodayDayNode(text)
    else -> RegularDayNode(text)
}

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
                onClick = { onAction(OnShowExerciseDialog(true)) }
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
            onSelect = { onAction(OnMuscleSelected(it.id)) }
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
                    onClick = { onAction(OnShowExerciseDialog(true)) }
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
            onSelect = { onAction(OnExerciseSelected(it.id)) }
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
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        when (state.screenMode) {
            is ScreenMode.Planning -> PlanningActionButtons(onAction = onAction, state = state)
            ScreenMode.Workout -> WorkoutActionButtons(onAction = onAction, state = state)
            ScreenMode.Review -> ReviewActionButtons(onAction = onAction, state = state)
        }
    }
}

@Composable
private fun RowScope.ReviewActionButtons(
    onAction: (ExerciseListAction) -> Unit,
    state: ExerciseListState
) {
    SecondaryButtonLayout(
        modifier = Modifier.weight(1f),
        onClick = { onAction(OnReturnToWorkout) },
        content = { ReturnToWorkoutButton() }
    )

    if (state.selectedExerciseIds.isNotEmpty()) {
        PrimaryButtonLayout(
            modifier = Modifier.weight(1f),
            onClick = { onAction(OnRedoWorkout) },
            content = { RedoWorkoutButton() }
        )
    }

    PrimaryButtonLayout(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        onClick = { onAction(OnShowDeleteWorkoutDialog(true)) },
        content = { DeleteButton() }
    )
}

@Composable
private fun RowScope.PlanningActionButtons(
    onAction: (ExerciseListAction) -> Unit,
    state: ExerciseListState
) {
    val isUpdate =
        state.screenMode is ScreenMode.Planning && state.screenMode.day in state.plannedWorkouts

    SecondaryButtonLayout(
        modifier = Modifier.weight(1f),
        onClick = { onAction(OnReturnToWorkout) },
        content = { ReturnToWorkoutButton() }
    )

    if (state.selectedExerciseIds.isNotEmpty()) {
        PrimaryButtonLayout(
            modifier = Modifier.weight(1f),
            onClick = { onAction(OnSaveWorkout(isUpdate)) },
            content = { SaveWorkoutButton(isUpdate) }
        )
    }

    if (state.daySelected in state.plannedWorkouts) {
        PrimaryButtonLayout(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            onClick = { onAction(OnShowDeletePlannedDialog(true)) },
            content = { DeleteButton() }
        )
    }
}

@Composable
private fun RowScope.WorkoutActionButtons(
    onAction: (ExerciseListAction) -> Unit,
    state: ExerciseListState
) {
    if (state.selectedExerciseIds.isNotEmpty()) {
        PrimaryButtonLayout(
            modifier = Modifier.weight(1f),
            onClick = { onAction(OnStartWorkout) },
            content = { StartWorkoutButton() }
        )
    }

    if (state.hasActiveWorkout) {
        PrimaryButtonLayout(
            modifier = Modifier.weight(1f),
            onClick = { onAction(OnResumeWorkout) },
            content = { ResumeWorkoutButton() }
        )
    }

    if (state.currentDay in state.workoutDaysDone) {
        PrimaryButtonLayout(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            onClick = { onAction(OnShowDeleteWorkoutDialog(true)) },
            content = { DeleteButton() }
        )
    }
}

@Composable
private fun PrimaryButtonLayout(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    FilledTonalButton(
        modifier = modifier,
        shapes = ButtonShapes(
            shape = MaterialTheme.shapes.medium,
            pressedShape = MaterialTheme.shapes.extraLarge
        ),
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = containerColor),
        onClick = onClick,
        content = content
    )
}

@Composable
private fun SecondaryButtonLayout(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        shapes = ButtonShapes(
            shape = MaterialTheme.shapes.medium,
            pressedShape = MaterialTheme.shapes.extraLarge
        ),
        onClick = onClick,
        content = content
    )
}

@Composable
private fun IconTextActionButton(icon: ImageVector, text: String) {
    Icon(
        imageVector = icon,
        contentDescription = null
    )
    Spacer(Modifier.width(8.dp))
    Text(
        modifier = Modifier.padding(vertical = 8.dp),
        text = text
    )
}

@Composable
private fun RedoWorkoutButton() {
    IconTextActionButton(
        icon = ImageVector.vectorResource(R.drawable.outline_redo_24),
        text = stringResource(R.string.redo_workout)
    )
}

@Composable
private fun SaveWorkoutButton(isUpdate: Boolean) {
    val text =
        if (isUpdate) R.string.update_workout
        else R.string.save_workout

    IconTextActionButton(
        icon = ImageVector.vectorResource(R.drawable.outline_calendar_month_24),
        text = stringResource(text)
    )
}

@Composable
private fun ReturnToWorkoutButton() {
    IconTextActionButton(
        icon = ImageVector.vectorResource(R.drawable.outline_chevron_backward_24),
        text = stringResource(id = R.string.return_to_workout)
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
private fun DeleteButton() {
    Icon(
        modifier = Modifier.padding(vertical = 6.dp),
        imageVector = ImageVector.vectorResource(R.drawable.outline_delete_24),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onTertiaryContainer
    )
}

@Composable
private fun StartWorkoutButton() {
    IconTextActionButton(
        icon = ImageVector.vectorResource(R.drawable.outline_exercise_24),
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
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    message: String,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = { onConfirm() }) {
                Text(stringResource(R.string.delete_workout_plan_dialog_delete_action))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
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
                Text(stringResource(R.string.cancel))
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

/**
 * RD: Regular day
 * T: Today
 * TT: This day
 * WD: Workout done
 * WC: Week done
 */
@Preview(uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DayNodesLightPreview() {
    ExerciseTrackerTheme {
        val data = mapOf(
            "Regular Day" to RegularDayNode("L"),
            "Today" to TodayDayNode("L"),
            "Workout" to WorkoutDayNode,
            "Week workout" to WeekAndWorkoutDayNode,
            "Planned" to PlannedWorkoutDayNode,
            "Planning" to PlanningWorkoutDayNode
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            data.forEach { (key, value) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.labelSmall
                    )
                    DayNode(
                        onClick = {},
                        node = value
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DayNodesPreview() {
    DayNodesLightPreview()
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ExerciseListScreenPreview() {
    ExerciseTrackerTheme {
        Surface {
            ExerciseListScreen(
                onAction = {},
                events = flowOf(),
                state = ExerciseListState(
                    screenMode = ScreenMode.Workout,
                    hasActiveWorkout = false,
                    workoutDaysDone = setOf(1),
                    currentDay = 1,
                    selectedExerciseIds = emptyList()
                )
            )
        }
    }
}

private fun showSnackBarMessage(
    scope: CoroutineScope,
    hostState: SnackbarHostState,
    message: String
) {
    scope.launch {
        hostState.currentSnackbarData?.dismiss()
        hostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short,
            withDismissAction = true
        )
    }
}