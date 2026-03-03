@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.exercisetracker.presentation.metrics

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.exercisetracker.R
import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.filter.TypeFilter
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.domain.model.WorkoutSet
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.VerticalIndicatorProperties
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
    Scaffold {
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
                    averageReps = state.averageReps,
                    maxWeight = state.maxWeight,
                    estimated1RM = state.rm
                )
            }

            if (state.graphPoints.isEmpty()) return@LazyColumn

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

            item { Graph(mode = state.typeFilterSelected, data = state.graphPoints) }

            item {
                HistoricalSetRow(
                    WorkoutSet(
                        id = 1,
                        sessionId = 1,
                        exerciseId = 1,
                        setNumber = 3,
                        weight = 23f,
                        reps = 2
                    )
                ) { }
            }
        }
    }
}

@Composable
fun SetInfoRow(
    setNumber: Int,
    weight: Float,
    reps: Int,
    unit: String = "lbs",
    isLastSet: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Set Number Indicator
        Surface(
            shape = CircleShape,
            color = Color(0xFF6650a4).copy(alpha = 0.1f), // Brand color light background
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$setNumber",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF6650a4), // Brand color text
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. Weight Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Weight",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$weight",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = " $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        // 3. Multiplication Sign (Visual Separator)
        Text(
            text = "×",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // 4. Reps Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Reps",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$reps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // This is where you'd put the Three-Dot menu icon we discussed!
    }

    if (!isLastSet) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 48.dp, top = 4.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
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
                        text = { Text(exercise.name.cap()) },
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
    averageReps: Int,
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
                    modifier = Modifier.weight(1f),
                    withLabel = false,
                    label = stringResource(R.string.average_reps),
                    value = "$averageReps",
                )
                StatItem(
                    Modifier.weight(1f),
                    label = stringResource(R.string.max_weight),
                    value = "$maxWeight",
                )
                StatItem(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.est_1rm),
                    value = String.format(getDefault(), "%.2f", estimated1RM),
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    withLabel: Boolean = true,
    label: String,
    value: String,
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

            if (withLabel) {
                Text(
                    text = stringResource(R.string.kg),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                TimeFilter.ONE_WEEK -> R.string.one_week
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
private fun Graph(
    modifier: Modifier = Modifier,
    mode: TypeFilter,
    data: Map<String, Double>
) {
    if (data.isEmpty()) return

    val dataLabel = when (mode) {
        TypeFilter.REPS -> stringResource(R.string.reps_small)
        TypeFilter.WEIGHT -> stringResource(R.string.kg)
    }

    val style = with(MaterialTheme.typography.labelMedium) {
        TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = fontSize,
            fontFamily = fontFamily
        )
    }

    val height = data.keys.size * 60

    RowChart(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .padding(top = 16.dp),
        data = data.map { (key, value) ->
            Bars(
                label = key,
                values = listOf(
                    Bars.Data(
                        value = value,
                        color = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.onSecondary,
                                MaterialTheme.colorScheme.primary,
                            )
                        )
                    )
                )
            )
        },
        barProperties = BarProperties(
            cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
            spacing = 3.dp,
            thickness = 20.dp
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        indicatorProperties = VerticalIndicatorProperties(
            textStyle = style
        ),
        labelProperties = LabelProperties(
            enabled = true,
            textStyle = style,
            labels = data.keys.toList()
        ),
        labelHelperProperties = LabelHelperProperties(
            enabled = false,
            textStyle = style
        ),
        popupProperties = PopupProperties(
            enabled = true,
            contentBuilder = { popUp ->
                "${String.format(getDefault(), "%.2f", popUp.value)} $dataLabel"
            },
            textStyle = style
        )
    )
}

@Composable
fun HistoricalSetRow(
    set: WorkoutSet,
    onDeleteConfirmed: (WorkoutSet) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        with(set) {
            SetInfoRow(
                setNumber = setNumber,
                weight = weight,
                reps = reps,
                unit = stringResource(R.string.kg),
                isLastSet = false
            )
        }

        Spacer(Modifier.weight(1f))

        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.outline_more_vert_24),
                contentDescription = "Options"
            )
        }

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Delete Set", color = Color.Red) },
                onClick = {
                    showMenu = false
                    showDialog = true
                }
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Historical Data?") },
            text = { Text("This will permanently remove this set and update your progress charts.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteConfirmed(set)
                    showDialog = false
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
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

