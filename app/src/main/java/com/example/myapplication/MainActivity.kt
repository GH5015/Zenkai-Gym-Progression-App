package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.Exercise
import com.example.myapplication.ui.navigation.AppDestinations
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.GymViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val vm: GymViewModel = viewModel()
                GymApp(vm)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Zenkai Timer"
            val descriptionText = "Notificações do cronômetro de descanso"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("ZENKAI_TIMER", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun GymApp(vm: GymViewModel) {
    var currentDest by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    val exercises by vm.allExercises.collectAsState(initial = emptyList())

    BackHandler(selectedExercise != null) {
        selectedExercise = null
    }

    NavigationSuiteScaffold(
        containerColor = MaterialTheme.colorScheme.background,
        navigationSuiteItems = {
            AppDestinations.entries.forEach { dest ->
                item(
                    icon = { 
                        Icon(
                            imageVector = if (currentDest == dest) dest.selectedIcon else dest.unselectedIcon, 
                            contentDescription = dest.label 
                        ) 
                    },
                    label = { Text(dest.label) },
                    selected = currentDest == dest,
                    onClick = { 
                        currentDest = dest
                        selectedExercise = null 
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (currentDest) {
                    AppDestinations.HOME -> {
                        if (selectedExercise == null) {
                            HomeScreen(
                                exercises = exercises,
                                onExerciseClick = { selectedExercise = it },
                                onDelete = { vm.deleteExercise(it) },
                                onAdd = { n, t, s, d, i -> vm.addExercise(n, t, s, d, i) },
                                onUpdate = { vm.updateExercise(it) },
                                vm = vm
                            )
                        } else {
                            val sets by vm.getSets(selectedExercise!!.id).collectAsState(initial = emptyList())
                            ExerciseDetailScreen(
                                exercise = selectedExercise!!,
                                sets = sets,
                                onAddSet = { w, r, t, q -> vm.addSet(selectedExercise!!.id, w, r, t, q) },
                                onDeleteSet = { vm.deleteSet(it) },
                                onBack = { selectedExercise = null }
                            )
                        }
                    }
                    AppDestinations.SCARS -> BattleScarsScreen(vm)
                    AppDestinations.PROGRESS -> ProgressScreen(exercises, vm)
                    AppDestinations.CALENDAR -> ConsistencyCalendarScreen(exercises, vm)
                    AppDestinations.REVIEW -> ReviewHubScreen(exercises, vm)
                    AppDestinations.PROFILE -> ProfileScreen(exercises, vm)
                }
            }
        }
    }
}
