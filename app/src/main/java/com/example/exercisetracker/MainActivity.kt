package com.example.exercisetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import com.example.exercisetracker.presentation.home.ExerciseListRoot
import com.example.exercisetracker.presentation.metrics.MetricsRoot
import com.example.exercisetracker.presentation.navigation.Navigator
import com.example.exercisetracker.presentation.navigation.Route
import com.example.exercisetracker.presentation.navigation.TOP_LEVEL_DESTINATION
import com.example.exercisetracker.presentation.navigation.appNavigationBar
import com.example.exercisetracker.presentation.navigation.rememberNavigationState
import com.example.exercisetracker.presentation.navigation.toEntries
import com.example.exercisetracker.presentation.review.ReviewRoot
import com.example.exercisetracker.presentation.workout.WorkoutSessionRoot
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val adaptiveInfo = currentWindowAdaptiveInfo()
            val layoutType = with(adaptiveInfo) {
                if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
                    NavigationSuiteType.NavigationRail
                } else {
                    NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(this)
                }
            }
            val navigationState = rememberNavigationState(
                startRoute = Route.Home,
                topLevelRoutes = TOP_LEVEL_DESTINATION.keys
            )

            val navigator = remember {
                Navigator(navigationState)
            }

            ExerciseTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    NavigationSuiteScaffold(
                        layoutType = layoutType,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        navigationSuiteItems = {
                            appNavigationBar(
                                selectedKey = navigationState.topLevelRoute,
                                onSelectKey = { navigator.navigate(it) }
                            )
                        },
                    ) {
                        NavDisplay(
                            modifier = Modifier.fillMaxSize(),
                            onBack = { navigator.goBack() },
                            entries = navigationState.toEntries(
                                entryProvider = entryProvider {
                                    entry<Route.Home> {
                                        ExerciseListRoot(
                                            navigator = navigator,
                                            viewModel = koinViewModel()
                                        )
                                    }

                                    entry<Route.Workout> {
                                        WorkoutSessionRoot(
                                            navigator = navigator,
                                            viewModel = koinViewModel()
                                        )
                                    }

                                    entry<Route.Metrics> {
                                        MetricsRoot(viewModel = koinViewModel())
                                    }

                                    entry<Route.Review> {
                                        ReviewRoot(
                                            viewModel = koinViewModel(
                                                parameters = { parametersOf(it.day) }
                                            )
                                        )
                                    }
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}