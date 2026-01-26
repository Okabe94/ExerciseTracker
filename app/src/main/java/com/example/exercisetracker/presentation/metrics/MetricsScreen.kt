@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.exercisetracker.presentation.metrics

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
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.filter.TypeFilter
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme
import com.example.exercisetracker.ui.theme.Orange500

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
                .padding(16.dp)
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
                Column {
                    FilterSection(
                        onFilterSelected = {},
                        filters = state.timeFilterOptions,
                        selected = state.timeFilterSelected
                    )
                    GraphMetricToggle(state.typeFilterSelected, {})
                    Graph()
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

                DayNode(
                    dayName = day,
                    isWorkoutDone = isWorkoutDone,
                    isToday = isToday
                )
            }
        }
    }
}

@Composable
private fun DayNode(dayName: String, isWorkoutDone: Boolean, isToday: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    color = when {
                        isWorkoutDone && isToday -> Orange500
                        isWorkoutDone -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                )
                .border(
                    width = if (isToday) 2.dp else 0.dp,
                    color = when {
                        isWorkoutDone && isToday -> Orange500
                        isWorkoutDone -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.surfaceVariant
                        else -> Color.Transparent
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isWorkoutDone) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_local_fire_department_24),
                    contentDescription = null,
                    tint =
                        if (isToday) Color.Black
                        else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (isToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

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
                    label = { Text(muscle.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { onExpandedChange(it) }
        ) {
            OutlinedTextField(
                value = selectedExercise?.name.orEmpty(),
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
    estimated1RM: Float,
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
                text = exerciseName.name,
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
                    value = "$estimated1RM",
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
    selectedMetric: TypeFilter,
    onMetricSelected: (Int) -> Unit
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

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        options.keys.forEachIndexed { index, key ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onMetricSelected(index) },
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
                Text(options.getValue(key).first, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun Graph(modifier: Modifier = Modifier) {
    val pointsData: List<Point> = listOf(
        Point(x = 0f, y = 40f),
        Point(1f, 90f),
        Point(1f, 0f),
        Point(0f, 60f),
        Point(4f, 10f)
    )
    val xAxisData = AxisData.Builder()
        .axisStepSize(60.dp)
        .backgroundColor(MaterialTheme.colorScheme.surfaceVariant)
        .axisLabelColor(MaterialTheme.colorScheme.onSurfaceVariant)
        .steps(pointsData.size - 1)
        .labelData { i -> pointsData[i].description }
        .labelAndAxisLinePadding(15.dp)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(10)
        .backgroundColor(MaterialTheme.colorScheme.surfaceVariant)
        .axisLabelColor(MaterialTheme.colorScheme.onSurfaceVariant)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            val yScale = 100 / 10f
            (i * yScale).formatToSinglePrecision()
        }.build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    lineStyle = LineStyle(),
                    intersectionPoint = IntersectionPoint(),
                    selectionHighlightPoint = SelectionHighlightPoint(),
                    shadowUnderLine = ShadowUnderLine(color = MaterialTheme.colorScheme.primary),
                    selectionHighlightPopUp = SelectionHighlightPopUp()
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

    LineChart(
        modifier = modifier.height(300.dp),
        lineChartData = lineChartData
    )
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
