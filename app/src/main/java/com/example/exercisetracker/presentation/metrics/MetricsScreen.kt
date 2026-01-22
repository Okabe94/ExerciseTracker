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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme

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
            WorkoutWeekCalendar(setOf(1, 4, 5), 7)
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
                    "Rows",
                    {},
                    listOf("Espalda", "Pecho", "Brazo", "Triceps"),
                    listOf("Rows", "Raise", "Calf", "Push up", "Pull up")
                )
            }
            item {
                ExerciseSummaryCard("Rows", 200.0, 150.0, 180.2, "kg")
            }
            item {
                Column {
                    FilterSection()
                    GraphMetricToggle(0, {})
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
    val days = listOf("M", "T", "W", "T", "F", "S", "S")

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
                    when {
                        isWorkoutDone -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                )
                .border(
                    width = if (isToday) 2.dp else 0.dp,
                    color =
                        if (isToday) MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isWorkoutDone) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_local_fire_department_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
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
    selectedExercise: String,
    onExerciseSelected: (String) -> Unit,
    muscleGroups: List<String>,
    exerciseList: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedMuscleGroup by remember { mutableStateOf("All") }

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
            items(listOf("All") + muscleGroups) { muscle ->
                FilterChip(
                    selected = selectedMuscleGroup == muscle,
                    onClick = { selectedMuscleGroup = muscle },
                    label = { Text(muscle) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedExercise,
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
                onDismissRequest = { expanded = false }
            ) {
                exerciseList.forEach { exercise ->
                    DropdownMenuItem(
                        text = { Text(exercise) },
                        onClick = {
                            onExerciseSelected(exercise)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseSummaryCard(
    exerciseName: String,
    totalVolume: Double,
    maxWeight: Double,
    estimated1RM: Double,
    unit: String
) {
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
                text = exerciseName,
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
                    unit = unit,
                    Modifier.weight(1f)
                )
                StatItem(
                    label = stringResource(R.string.max_weight),
                    value = "$maxWeight",
                    unit = unit,
                    Modifier.weight(1f)
                )
                StatItem(
                    label = stringResource(R.string.est_1rm),
                    value = "$estimated1RM",
                    unit = unit,
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
    unit: String,
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
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilterSection(modifier: Modifier = Modifier) {
    val selected by remember { mutableIntStateOf(0) }
    val filters = listOf(
        R.string.one_month,
        R.string.three_months,
        R.string.six_months,
        R.string.one_year,
        R.string.historic,
    )
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        itemsIndexed(filters) { index, item ->
            FilterChip(
                selected = index == selected,
                onClick = { index == selected },
                label = { Text(stringResource(item)) },
            )
        }
    }
}

@Composable
private fun GraphMetricToggle(
    selectedMetric: Int,
    onMetricSelected: (Int) -> Unit
) {
    val options = listOf(stringResource(R.string.reps), stringResource(R.string.weight))
    val icons = listOf(
        ImageVector.vectorResource(R.drawable.outline_scale_24),
        ImageVector.vectorResource(R.drawable.outline_repeat_24)
    )

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onMetricSelected(index) },
                selected = index == selectedMetric,
                icon = {
                    SegmentedButtonDefaults.Icon(active = index == selectedMetric) {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = null,
                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                        )
                    }
                }
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium)
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
        .labelData { i -> pointsData[i].description}
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
