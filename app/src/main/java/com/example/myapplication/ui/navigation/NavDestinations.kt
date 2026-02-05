package com.example.myapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Dojo", Icons.Rounded.FitnessCenter, Icons.Rounded.FitnessCenter),
    SCARS("Cicatrizes", Icons.Rounded.AutoAwesome, Icons.Rounded.AutoAwesome),
    PROGRESS("Evolução", Icons.Rounded.Timeline, Icons.Rounded.Timeline),
    CALENDAR("Calendário", Icons.Rounded.CalendarMonth, Icons.Rounded.Event),
    REVIEW("Análise", Icons.Rounded.Analytics, Icons.Rounded.Analytics),
    PROFILE("Perfil", Icons.Rounded.Person, Icons.Rounded.Person)
}
