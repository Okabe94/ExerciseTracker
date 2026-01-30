@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.example.exercisetracker.presentation.home

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.example.exercisetracker.core.presentation.util.ObserveAsEvents
import com.example.exercisetracker.presentation.home.PrimaryButtonLayout
import com.example.exercisetracker.presentation.home.SecondaryButtonLayout
import com.example.exercisetracker.presentation.navigation.Navigator
import com.example.exercisetracker.presentation.navigation.Route
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.example.exercisetracker.ui.theme.Gold400
import com.example.exercisetracker.ui.theme.Gold500
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
                is ExerciseListAction.OnStartWorkout,
                is ExerciseListAction.OnResumeWorkout -> navigator.navigate(Route.Workout)

                is ExerciseListAction.OnNavigateToReview -> navigator.navigate(Route.Review(it.day))
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
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ObserveAsEvents(events) { event ->
        when (event) {
            is ExerciseListEvent.ErrorMessage -> showSnackBarMessage(
                scope = scope,
                hostState = snackbarHostState,
                message = event.reason.asString(context)
            )

            is ExerciseListEvent.SendToReview -> onAction(
                ExerciseListAction.OnNavigateToReview(event.day)
            )
        }
    }

    Scaffold(
        modifier = Modifier.padding(16.dp),
        topBar = {
            WorkoutWeekCalendar(
                onDayNodeClick = { onAction(ExerciseListAction.OnDayNodeSelected(it)) },
//                onBackClick = { onAction(ExerciseListAction.OnReturnToWorkout) },
                workoutDays = state.workoutDaysDone,
                currentDay = state.currentDay,
                mode = state.screenMode
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
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
    onDayNodeClick: (Int) -> Unit,
    workoutDays: Set<Int>,
    currentDay: Int,
    mode: ScreenMode
) {
    val days = listOf("L", "M", "X", "J", "V", "S", "D")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        WeekWorkoutMode(
            onClick = onDayNodeClick,
            screenMode = mode,
            days = days,
            workoutDays = workoutDays,
            currentDay = currentDay
        )
    }
}

//@Composable
//fun WeekPlanningMode(onBack: () -> Unit, day: String) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.Start
//    ) {
//        IconButton(onClick = onBack) {
//            Icon(
//                imageVector = ImageVector.vectorResource(R.drawable.outline_arrow_back_24),
//                contentDescription = "back"
//            )
//        }
//
//        Text(
//            text = stringResource(R.string.planning),
//            style = MaterialTheme.typography.headlineMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//
//        Spacer(modifier = Modifier.width(12.dp))
//
//        DayNode(
//            onClick = {},
//            dayName = day,
//            isWorkoutDone = false,
//            isToday = false,
//            isWeekDone = false
//        )
//    }
//}

@Composable
fun WeekWorkoutMode(
    onClick: (Int) -> Unit,
    screenMode: ScreenMode,
    days: List<String>,
    workoutDays: Set<Int>,
    currentDay: Int
) {
    val title = when (screenMode) {
        is ScreenMode.Planning -> R.string.planning
        ScreenMode.Workout -> R.string.activity_this_week
    }

    Box {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

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
                onClick = { onClick(dayNumber) },
                dayName = day,
                isPlanning = screenMode is ScreenMode.Planning && screenMode.day == dayNumber,
                isWorkoutDone = isWorkoutDone,
                isToday = isToday,
                isWeekDone = isWeekDone
            )
        }
    }
}

@Composable
private fun DayNode(
    onClick: () -> Unit,
    dayName: String,
    isPlanning: Boolean,
    isWorkoutDone: Boolean,
    isToday: Boolean,
    isWeekDone: Boolean
) {
    val node = getDayNodeColors(isWorkoutDone, isToday, isWeekDone)
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val planningBackground = if (isPlanning) {
        Modifier
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    ),
                    tileMode = TileMode.Decal
                ),
                shape = RoundedCornerShape(5.dp),
                alpha = alpha
            )
    } else {
        Modifier
    }

    Column(
        modifier = planningBackground
            .then(Modifier.padding(4.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color = node.backgroundColor)
                .clickable { onClick() }
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
        when (state.screenMode) {
            is ScreenMode.Planning -> PlanningActionButtons(onAction = onAction, state = state)
            ScreenMode.Workout -> WorkoutActionButtons(onAction = onAction, state = state)
        }
    }
}

@Composable
fun RowScope.PlanningActionButtons(
    onAction: (ExerciseListAction) -> Unit,
    state: ExerciseListState
) {
    if (state.selectedExerciseIds.isEmpty()){
        PrimaryButtonLayout(
            onClick = { onAction(ExerciseListAction.OnReturnToWorkout) }
        ) { ReturnToWorkoutButton() }
    } else {
        SecondaryButtonLayout(
            onClick = { onAction(ExerciseListAction.OnReturnToWorkout) }
        ) { ReturnToWorkoutButton() }
        PrimaryButtonLayout(
            onClick = { onAction(ExerciseListAction.OnSaveWorkout) }
        ) { SaveWorkoutButton() }
    }
}

@Composable
fun RowScope.WorkoutActionButtons(
    onAction: (ExerciseListAction) -> Unit,
    state: ExerciseListState
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
private fun SaveWorkoutButton() {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.outline_calendar_month_24),
        contentDescription = null
    )
    Spacer(Modifier.width(8.dp))
    Text(
        modifier = Modifier.padding(vertical = 8.dp),
        text = stringResource(id = R.string.save_workout)
    )
}

@Composable
private fun ReturnToWorkoutButton() {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.outline_chevron_backward_24),
        contentDescription = null
    )
    Spacer(Modifier.width(8.dp))
    Text(
        modifier = Modifier.padding(vertical = 8.dp),
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
            "RD" to Triple(false, false, false),
            "T" to Triple(false, true, false),
            "WD TT" to Triple(true, false, false),
            "WD T" to Triple(true, true, false),
            "WC T" to Triple(false, true, true),
            "WC RD" to Triple(false, false, true),
            "WC TT" to Triple(true, false, true),
            "WC WD TT" to Triple(true, false, true),
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
                        isPlanning = true,
                        dayName = "L",
                        isWorkoutDone = value.first,
                        isToday = value.second,
                        isWeekDone = value.third
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
                    hasActiveWorkout = false,
                    selectedExerciseIds = listOf(1)
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