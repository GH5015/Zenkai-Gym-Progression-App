package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ExerciseSet
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.viewmodel.GymViewModel
import kotlinx.coroutines.flow.first

@Composable
fun ConsistencyCalendarScreen(exercises: List<com.example.myapplication.data.Exercise>, vm: GymViewModel) {
    val allSets = remember { mutableStateListOf<ExerciseSet>() }
    
    LaunchedEffect(exercises) {
        allSets.clear()
        exercises.forEach { ex ->
            vm.getSets(ex.id).first().forEach { allSets.add(it) }
        }
    }
    
    val days = allSets.map { it.date }.distinct().sortedDescending()
    
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            SectionHeader(
                title = "Consistência",
                subtitle = "Seu histórico de dedicação"
            )
            Spacer(Modifier.height(8.dp))
        }
        
        if (days.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Nenhum treino registrado ainda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(days) { date ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "Treino Finalizado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                date,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}
