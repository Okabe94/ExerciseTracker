package com.example.exercisetracker.presentation.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.exercisetracker.R
import com.example.exercisetracker.ui.theme.Gold400
import com.example.exercisetracker.ui.theme.Gold500

sealed interface IDayNode {
    @Composable
    fun GetNode()
}

sealed class BasicDayNode(
    val containerModifier: @Composable () -> Modifier,
    val backgroundColor: @Composable () -> Color,
    val borderColor: @Composable () -> Color,
    val borderSize: Dp,
) : IDayNode

sealed class TextDayNodeVisuals(
    val text: String,
    val textColor: @Composable () -> Color,
    borderSize: Dp,
    containerModifier: @Composable () -> Modifier,
    backgroundColor: @Composable (() -> Color),
    borderColor: @Composable (() -> Color),
) : BasicDayNode(
    containerModifier = containerModifier,
    backgroundColor = backgroundColor,
    borderColor = borderColor,
    borderSize = borderSize
) {
    @Composable
    override fun GetNode() {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor()
        )
    }
}

sealed class IconDayNodeVisuals(
    @DrawableRes val icon: Int,
    val tintColor: @Composable (() -> Color),
    containerModifier: @Composable () -> Modifier,
    backgroundColor: @Composable (() -> Color),
    borderColor: @Composable (() -> Color),
    borderSize: Dp
) : BasicDayNode(
    containerModifier = containerModifier,
    backgroundColor = backgroundColor,
    borderColor = borderColor,
    borderSize = borderSize
) {
    @Composable
    override fun GetNode() {
        Icon(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = null,
            tint = tintColor(),
            modifier = Modifier.size(20.dp)
        )
    }
}

data class RegularDayNode(val day: String) : TextDayNodeVisuals(
    text = day,
    borderSize = 0.dp,
    containerModifier = { Modifier },
    textColor = { MaterialTheme.colorScheme.onSurfaceVariant },
    backgroundColor = { MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f) },
    borderColor = { Color.Transparent },
)

data class TodayDayNode(val day: String) : TextDayNodeVisuals(
    text = day,
    containerModifier = { Modifier },
    backgroundColor = { MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f) },
    borderColor = { MaterialTheme.colorScheme.primary },
    textColor = { MaterialTheme.colorScheme.onSurfaceVariant },
    borderSize = 2.dp,
)

data object WorkoutDayNode : IconDayNodeVisuals(
    icon = R.drawable.outline_local_fire_department_24,
    containerModifier = { Modifier },
    backgroundColor = { MaterialTheme.colorScheme.primary },
    borderColor = { MaterialTheme.colorScheme.primary },
    tintColor = { MaterialTheme.colorScheme.onPrimary },
    borderSize = 0.dp,
)

data object WeekAndWorkoutDayNode : IconDayNodeVisuals(
    icon = R.drawable.outline_local_fire_department_24,
    containerModifier = { Modifier },
    backgroundColor = { Gold500 },
    borderColor = { Gold400 },
    tintColor = { Color.Black },
    borderSize = 2.dp,
)

data object PlannedWorkoutDayNode : IconDayNodeVisuals(
    icon = R.drawable.outline_calendar_month_24,
    containerModifier = { Modifier },
    backgroundColor = { MaterialTheme.colorScheme.surfaceVariant },
    borderColor = { MaterialTheme.colorScheme.onSurfaceVariant },
    tintColor = { MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f) },
    borderSize = 2.dp,
)

data object PlanningWorkoutDayNode : IconDayNodeVisuals(
    icon = R.drawable.outline_calendar_month_24,
    containerModifier = {
        val alpha by rememberInfiniteTransition().animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Modifier
            .graphicsLayer {
                rotationY = 360f * alpha * 2
            }
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
    },
    backgroundColor = { MaterialTheme.colorScheme.surfaceVariant },
    borderColor = { MaterialTheme.colorScheme.primary },
    tintColor = { MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f) },
    borderSize = 2.dp,
)


