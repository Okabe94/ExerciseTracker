package com.example.exercisetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.exercisetracker.presentation.home.ExerciseListRoot
import com.example.exercisetracker.presentation.home.ExerciseListScreen
import com.example.exercisetracker.presentation.workout.WorkoutSessionScreen
import com.example.exercisetracker.presentation.navigation.Route
import com.example.exercisetracker.ui.theme.ExerciseTrackerTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExerciseTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val backStack = rememberNavBackStack(Route.Home)
                    NavDisplay(
                        modifier = Modifier.padding(innerPadding),
                        backStack = backStack,
                        entryProvider = { key ->
                            when (key) {
                                is Route.Home -> NavEntry(key) {
                                    ExerciseListRoot(
                                        backStack = backStack,
                                        viewModel = koinViewModel()
                                    )
                                }

                                is Route.Workout -> NavEntry(key) {
                                    WorkoutSessionScreen(
                                        exerciseIds = key.exerciseIds.toSet(),
                                        onFinish = { backStack.removeAt(backStack.lastIndex) }
                                    )
                                }

                                else -> error("Unknown navkey")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ExerciseTrackerTheme {
        Greeting("Android")
    }
}
