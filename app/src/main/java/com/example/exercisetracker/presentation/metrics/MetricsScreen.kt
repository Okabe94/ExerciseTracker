@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.exercisetracker.presentation.metrics

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.axis.AxisData
import co.yml.charts.common.extensions.formatToSinglePrecision
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.example.exercisetracker.R
import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.filter.TypeFilter
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.example.exercisetracker.ui.theme.Gold400
import com.example.exercisetracker.ui.theme.Gold500
import java.util.Locale
import java.util.Locale.getDefault

@Composable
fun MetricsRoot(
    viewModel: MetricsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MetricsScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun MetricsScreen(
    state: MetricsState,
    onAction: (MetricsAction) -> Unit,
) {
    Scaffold(
        topBar = {
            WorkoutWeekCalendar(
                workoutDays = state.workoutDaysDone,
                currentDay = state.currentDay
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
        ) {
            item {
                ExerciseSelectorHeader(
                    selectedExercise = state.selectedExercise,
                    selectedMuscleId = state.filteredMuscleId,
                    onMuscleSelected = { muscle ->
                        onAction(MetricsAction.OnMuscleSelected(muscle))
                    },
                    onExerciseSelected = { exercise ->
                        onAction(MetricsAction.OnExerciseSelected(exercise))
                    },
                    onExpandedChange = { expanded ->
                        onAction(MetricsAction.OnExpandedChange(expanded))
                    },
                    muscleGroups = state.muscleList,
                    exerciseList = state.exerciseList,
                    expanded = state.expandedExerciseSelection
                )
            }

            item {
                ExerciseSummaryCard(
                    exerciseName = state.selectedExercise,
                    totalVolume = state.totalVolume,
                    maxWeight = state.maxWeight,
                    estimated1RM = state.rm
                )
            }

            item {
                FilterSection(
                    onFilterSelected = { onAction(MetricsAction.OnTimeSelected(it)) },
                    filters = state.timeFilterOptions,
                    selected = state.timeFilterSelected
                )
            }

            item {
                GraphMetricToggle(
                    selectedMetric = state.typeFilterSelected,
                    onMetricSelected = { action ->
                        onAction(MetricsAction.OnTypeSelected(action))
                    }
                )
            }

            item { Graph(data = state.graphPoints) }
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
            .padding(16.dp)
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
private fun ExerciseSelectorHeader(
    selectedExercise: Exercise?,
    selectedMuscleId: Int,
    onMuscleSelected: (Muscle) -> Unit,
    onExerciseSelected: (Exercise) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    muscleGroups: List<Muscle>,
    exerciseList: List<Exercise>,
    expanded: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = stringResource(R.string.select_exercise),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(muscleGroups) { muscle ->
                FilterChip(
                    selected = selectedMuscleId == muscle.id,
                    onClick = { onMuscleSelected(muscle) },
                    label = { Text(muscle.name.cap()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { onExpandedChange(it) }
        ) {
            OutlinedTextField(
                value = selectedExercise?.name.orEmpty().cap(),
                onValueChange = {},
                readOnly = true,
                placeholder = { Text(stringResource(R.string.choose_an_exercise)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                exerciseList.forEach { exercise ->
                    DropdownMenuItem(
                        text = { Text(exercise.name) },
                        onClick = { onExerciseSelected(exercise) },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseSummaryCard(
    exerciseName: Exercise?,
    totalVolume: Float,
    maxWeight: Float,
    estimated1RM: Double,
) {
    if (exerciseName == null) return

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = exerciseName.name.cap(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = stringResource(R.string.total_volume),
                    value = "$totalVolume",
                    Modifier.weight(1f)
                )
                StatItem(
                    label = stringResource(R.string.max_weight),
                    value = "$maxWeight",
                    Modifier.weight(1f)
                )
                StatItem(
                    label = stringResource(R.string.est_1rm),
                    value = String.format(Locale.getDefault(), "%.2f", estimated1RM),
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = stringResource(R.string.kg),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilterSection(
    onFilterSelected: (TimeFilter) -> Unit,
    filters: List<TimeFilter>,
    selected: TimeFilter
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        items(filters) { item ->
            val res = when (item) {
                TimeFilter.ALL -> R.string.everything
                TimeFilter.ONE_MONTH -> R.string.one_month
                TimeFilter.THREE_MONTH -> R.string.three_months
                TimeFilter.SIX_MONTH -> R.string.six_months
                TimeFilter.ONE_YEAR -> R.string.one_year
            }
            FilterChip(
                selected = item == selected,
                onClick = { onFilterSelected(item) },
                label = { Text(stringResource(res)) },
            )
        }
    }
}

@Composable
private fun GraphMetricToggle(
    onMetricSelected: (TypeFilter) -> Unit,
    selectedMetric: TypeFilter,
) {
    val options = mapOf(
        TypeFilter.WEIGHT to Pair(
            stringResource(R.string.weight),
            ImageVector.vectorResource(R.drawable.outline_scale_24)
        ),
        TypeFilter.REPS to Pair(
            stringResource(R.string.reps),
            ImageVector.vectorResource(R.drawable.outline_repeat_24)
        )
    )

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.keys.forEachIndexed { index, key ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onMetricSelected(key) },
                selected = key == selectedMetric,
                icon = {
                    SegmentedButtonDefaults.Icon(active = key == selectedMetric) {
                        Icon(
                            imageVector = options.getValue(key).second,
                            contentDescription = null,
                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                        )
                    }
                }
            ) {
                Text(
                    text = options.getValue(key).first,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun Graph(modifier: Modifier = Modifier, data: List<GraphPoints>) {
    if (data.isEmpty()) return

    val mapped = data.mapIndexed { index, point ->
        Point(x = index.toFloat() + 1, y = point.value, description = point.description)
    }

    val points = listOf(Point(0f, 0f, "")) + mapped
    val maxValue = points.maxOfOrNull { it.y } ?: 0f

    val xAxisData = AxisData.Builder()
        .axisStepSize(60.dp)
        .backgroundColor(MaterialTheme.colorScheme.surfaceVariant)
        .axisLabelColor(MaterialTheme.colorScheme.onSurfaceVariant)
        .steps(points.size - 1)
        .labelData { i -> points[i].description }
        .labelAndAxisLinePadding(20.dp)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(10)
        .backgroundColor(MaterialTheme.colorScheme.surfaceVariant)
        .axisLabelColor(MaterialTheme.colorScheme.onSurfaceVariant)
        .labelAndAxisLinePadding(30.dp)
        .labelData { index -> (index * (maxValue / 10)).formatToSinglePrecision() }
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = points,
                    lineStyle = LineStyle(),
                    intersectionPoint = IntersectionPoint(),
                    selectionHighlightPoint = SelectionHighlightPoint(color = MaterialTheme.colorScheme.tertiary),
                    shadowUnderLine = ShadowUnderLine(color = MaterialTheme.colorScheme.primary),
                    selectionHighlightPopUp = SelectionHighlightPopUp(popUpLabel = { x, y -> " $y " })
                )
            ),
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            lineWidth = 0.3.dp
        ),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    )

    Card {
        LineChart(
            modifier = modifier.height(300.dp),
            lineChartData = lineChartData
        )
    }
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

@Preview(uiMode = UI_MODE_NIGHT_YES)
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
private fun MetricsScreenPreview() {
    ExerciseTrackerTheme {
        MetricsScreen(
            state = sampleMetricsState,
            onAction = {}
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    ExerciseTrackerTheme {
        MetricsScreen(
            state = MetricsState(),
            onAction = {}
        )
    }
}

private fun String.cap(): String = this.replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(
        getDefault()
    ) else it.toString()
}

