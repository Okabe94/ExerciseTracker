@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.example.exercisetracker.presentation.metrics

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.ButtonGroupDefaults
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
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.axis.AxisData
import co.yml.charts.common.extensions.formatToSinglePrecision
import co.yml.charts.common.model.PlotType
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
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
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
//            DaySection()
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
//                SelectMuscleSection()
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
fun WorkoutWeekCalendar(
    workoutDays: Set<Int>, // e.g., Set of 1 (Monday), 3 (Wednesday)...
    currentDay: Int = 3    // Wednesday
) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Activity This Week",
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
fun DayNode(dayName: String, isWorkoutDone: Boolean, isToday: Boolean) {
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
                    color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
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
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphMetricToggle(
    selectedMetric: Int,
    onMetricSelected: (Int) -> Unit
) {
    val options = listOf("Weight", "Reps")
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
fun Test(modifier: Modifier = Modifier) {
    val options = listOf(stringResource(R.string.reps), stringResource(R.string.weight))
    val unCheckedIcons = listOf(
        ImageVector.vectorResource(R.drawable.outline_check_24),
        ImageVector.vectorResource(R.drawable.outline_check_24),
    )
    val checkedIcons = listOf(
        ImageVector.vectorResource(R.drawable.outline_check_24),
        ImageVector.vectorResource(R.drawable.outline_check_24),
    )
    var selectedIndex by remember { mutableIntStateOf(0) }

    Row(
        Modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        val modifiers = listOf(
            Modifier.weight(1f),
            Modifier.weight(1f)
        )

        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = { selectedIndex = index },
                modifier = modifiers[index].semantics { role = Role.RadioButton },
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
            ) {
                Icon(
                    if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index],
                    contentDescription = "Localized description",
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(label)
            }
        }
    }
}

@Composable
fun ExerciseSelectorHeader(
    selectedExercise: String,
    onExerciseSelected: (String) -> Unit,
    muscleGroups: List<String>,
    exerciseList: List<String> // This should be filtered based on the selected muscle group
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedMuscleGroup by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = "Select Exercise",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Muscle Group Filter Chips
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

        // 2. Searchable Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedExercise,
                onValueChange = {}, // Read-only or add search logic here
                readOnly = true,
                placeholder = { Text("Choose an exercise...") },
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
fun ExerciseSummaryCard(
    exerciseName: String,
    totalVolume: Double,
    maxWeight: Double,
    estimated1RM: Double,
    unit: String = "lbs"
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
            // Header: Exercise Name
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

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total Volume",
                    value = "$totalVolume",
                    unit = unit,
                    Modifier.weight(1f)
                )
                StatItem(
                    label = "Max Weight",
                    value = "$maxWeight",
                    unit = unit,
                    Modifier.weight(1f)
                )
                StatItem(
                    label = "Est. 1RM",
                    value = "$estimated1RM",
                    unit = unit,
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatItem(
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
fun FilterSection(modifier: Modifier = Modifier) {
    val filters = mapOf(
        R.string.one_month to false,
        R.string.three_months to false,
        R.string.six_months to true,
        R.string.one_year to false,
        R.string.historic to false,
    )
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        items(filters.toList()) {
            FilterChip(
                selected = it.second,
                onClick = {},
                label = { Text(stringResource(it.first)) },
                trailingIcon = {
                    if (it.second) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.outline_check_24),
                            null
                        )
                    }
                },
            )
        }
    }
}

@Composable
fun SelectMuscleSection(modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Ejercicio")
        Icon(
            ImageVector.vectorResource(R.drawable.outline_arrow_drop_down_24),
            null
        )
    }
}

@Composable
fun DaySection(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Entrenos semanales",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(Modifier.height(8.dp))

        val days = listOf("L", "M", "X", "J", "V", "S", "D")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            days.forEach {
                Box {
                    Box(
                        modifier = Modifier
                            .background(color = Color.Red, shape = CircleShape)
                            .size(4.dp)
                            .align(Alignment.BottomCenter),
                    )
                    Text(it)
                }
            }
        }
    }
}

@Composable
private fun PieChartSection() {
    val chartData = PieChartData(
        listOf(
            PieChartData.Slice(
                label = "Label",
                value = 30f,
                color = Color.Blue
            ),
            PieChartData.Slice(
                label = "Label",
                value = 10f,
                color = Color.Black
            ),
            PieChartData.Slice(
                label = "Label",
                value = 20f,
                color = Color.Red
            )
        ), PlotType.Pie
    )
    val chartConfig = PieChartConfig()
    PieChart(
        modifier = Modifier.fillMaxSize(),
        pieChartData = chartData,
        pieChartConfig = chartConfig
    )
}

@Composable
private fun Graph(modifier: Modifier = Modifier) {
    val pointsData: List<Point> = listOf(
        Point(x = 0f, y = 40f),
        Point(1f, 90f),
        Point(2f, 0f),
        Point(3f, 60f),
        Point(4f, 10f)
    )
    val xAxisData = AxisData.Builder()
        .axisStepSize(60.dp)
        .backgroundColor(MaterialTheme.colorScheme.surfaceVariant)
        .axisLabelColor(MaterialTheme.colorScheme.onSurfaceVariant)
        .steps(pointsData.size - 1)
        .labelData { i -> i.toString() }
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
        modifier = Modifier.height(300.dp),
        lineChartData = lineChartData
    )

}

@Composable
private fun RepsSection(modifier: Modifier = Modifier) {

}

@Composable
private fun WeightPerSessionSection(modifier: Modifier = Modifier) {

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
