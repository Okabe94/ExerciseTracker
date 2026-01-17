package com.example.exercisetracker.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation3.runtime.NavKey

fun NavigationSuiteScope.appNavigationBar(
    selectedKey: NavKey,
    onSelectKey: (NavKey) -> Unit,
) {
    TOP_LEVEL_DESTINATION.forEach { (topDestination, data) ->
        item(
            selected = topDestination == selectedKey,
            onClick = { onSelectKey(topDestination) },
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(data.icon),
                    contentDescription = null
                )
            },
            label = {
                Text(stringResource(data.title))
            }
        )
    }
}