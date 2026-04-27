@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.exercisetracker.presentation.metrics

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.exercisetracker.R
import com.example.exercisetracker.core.presentation.util.cap
import com.example.exercisetracker.data.local.model.MetricGraphData
import com.example.exercisetracker.domain.filter.TimeFilter
import com.example.exercisetracker.domain.filter.TypeFilter
import com.example.exercisetracker.domain.model.Exercise
import com.example.exercisetracker.domain.model.Muscle
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.VerticalIndicatorProperties
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
                OverallActivityCard(
                    totalWorkouts = state.totalWorkoutsAllTime,
                    thisWeek = state.workoutsThisWeek,
                    avgPerWeek = state.avgWorkoutsPerWeek
                )
            }

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

            if (state.graphPoints.isEmpty()) {
                item { EmptyDataSection() }
                return@LazyColumn
            }

            item {
                ExerciseSummaryCard(
                    exerciseName = state.selectedExercise,
                    averageReps = state.averageReps,
                    maxWeight = state.maxWeight,
                    estimated1RM = state.rm,
                    totalVolume = state.totalVolume,
                    totalSessions = state.totalSessions,
                    totalSets = state.totalSets,
                    prWeight = state.prWeight,
                    prReps = state.prReps,
                    prDate = state.prDate
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

            item { Graph(mode = state.typeFilterSelected, data = state.graphPoints) }

            state.groupedSets.forEach { (label, sets) ->
                val isExpanded = state.expandedSets.contains(label)
                item {
                    ExpandableSetView(
                        onClick = { onAction(MetricsAction.OnToggleExpandSet(it)) },
                        label = label,
                        isExpanded = isExpanded
                    )
                }
                if (isExpanded) {
                    items(sets) { set ->
                        MetricSetRow(
                            set = set,
                            setNumber = sets.indexOf(set) + 1,
                            onDeleteClick = { onAction(MetricsAction.OnShowDeleteConfirmation(it)) }
                        )
                    }
                }
            }
        }

        if (state.showDeleteConfirmation && state.setIdToDelete != null) {
            DeleteConfirmationDialog(
                onDismiss = { onAction(MetricsAction.OnShowDeleteConfirmation(null)) },
                onConfirm = { onAction(MetricsAction.OnDeleteSet(state.setIdToDelete)) }
            )
        }
    }
}

@Composable
fun ExpandableSetView(
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
    label: String,
    isExpanded: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(label) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = if (isExpanded)
                ImageVector.vectorResource(R.drawable.outline_arrow_drop_up_24)
            else
                ImageVector.vectorResource(R.drawable.outline_arrow_drop_down_24),
            contentDescription = if (isExpanded) "Collapse" else "Expand"
        )
    }
}

@Composable
private fun MetricSetRow(
    set: MetricGraphData,
    setNumber: Int,
    onDeleteClick: (Int) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$setNumber",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.weight),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${set.weight}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = " ${stringResource(R.string.kg)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.reps),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${set.reps}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.outline_more_vert_24),
                    contentDescription = "Options"
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDeleteClick(set.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_set_dialog_title)) },
        text = { Text(stringResource(R.string.delete_set_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(id = R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun SetInfoRow(
    setNumber: Int,
    weight: Float,
    reps: Int,
    unit: String = "lbs",
    isLastSet: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

    }

}

@Composable
private fun OverallActivityCard(
    totalWorkouts: Int,
    thisWeek: Int,
    avgPerWeek: Double
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                modifier = Modifier.weight(1f),
                withLabel = false,
                label = stringResource(R.string.total_workouts_label),
                value = "$totalWorkouts"
            )
            StatItem(
                modifier = Modifier.weight(1f),
                withLabel = false,
                label = stringResource(R.string.this_week_label),
                value = "$thisWeek"
            )
            StatItem(
                modifier = Modifier.weight(1f),
                withLabel = false,
                label = stringResource(R.string.avg_per_week_label),
                value = String.format(getDefault(), "%.1f", avgPerWeek)
            )
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
            text = stringResource(R.string.muscles_filter),
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
    totalVolume: Double,
    totalSessions: Int,
    totalSets: Int,
    prWeight: Float,
    prReps: Int,
    prDate: String
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
                    label = stringResource(R.string.total_volume_label),
                    value = String.format(getDefault(), "%.1f", totalVolume),
                )
                StatItem(
                    modifier = Modifier.weight(1f),
                    withLabel = false,
                    label = stringResource(R.string.sessions_count_label),
                    value = "$totalSessions",
                )
                StatItem(
                    modifier = Modifier.weight(1f),
                    withLabel = false,
                    label = stringResource(R.string.sets_count_label),
                    value = "$totalSets",
                )
            }

            if (prWeight > 0f) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = stringResource(R.string.personal_record_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$prWeight ${stringResource(R.string.kg)} × $prReps ${stringResource(R.string.reps_small)} — $prDate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
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
            stringResource(R.string.graph_weight_label),
            ImageVector.vectorResource(R.drawable.outline_scale_24)
        ),
        TypeFilter.REPS to Pair(
            stringResource(R.string.graph_reps_label),
            ImageVector.vectorResource(R.drawable.outline_repeat_24)
        ),
        TypeFilter.VOLUME to Pair(
            stringResource(R.string.volume_type_label),
            ImageVector.vectorResource(R.drawable.outline_data_usage_24)
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
private fun EmptyDataSection(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(top = 24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = ImageVector.vectorResource(R.drawable.outline_data_usage_24),
                contentDescription = null
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.metrics_empty_section_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
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
        TypeFilter.VOLUME -> stringResource(R.string.kg)
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
            .padding(top = 8.dp),
        data = data.map { (key, value) ->
            Bars(
                label = key,
                values = listOf(
                    Bars.Data(
                        value = value,
                        color = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.tertiary,
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
            textStyle = style,
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
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

