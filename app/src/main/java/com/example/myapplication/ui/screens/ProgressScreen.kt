package com.example.myapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Exercise
import com.example.myapplication.data.ExerciseSet
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.utils.calculate1RM
import com.example.myapplication.viewmodel.GymViewModel

enum class DataType(val label: String) { 
    CARGA("Carga"), 
    REPS("Reps"), 
    ONE_RM("1RM Est.") 
}

@Composable
fun ProgressScreen(exercises: List<Exercise>, vm: GymViewModel) {
    var type by remember { mutableStateOf(DataType.CARGA) }
    
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            SectionHeader(
                title = "Evolução",
                subtitle = "Acompanhe seu progresso ao longo do tempo"
            )
            
            ScrollableTabRow(
                selectedTabIndex = type.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    if (type.ordinal < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[type.ordinal]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                divider = {}
            ) {
                DataType.entries.forEach { entry ->
                    Tab(
                        selected = type == entry,
                        onClick = { type = entry },
                        text = { 
                            Text(
                                entry.label, 
                                fontWeight = if (type == entry) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) 
                        }
                    )
                }
            }
        }
        
        items(exercises) { ex ->
            val sets by vm.getSets(ex.id).collectAsState(initial = emptyList())
            if (sets.size >= 2) {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                ex.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val latestValue = when(type) {
                                DataType.CARGA -> sets.last().weight
                                DataType.REPS -> sets.last().reps
                                DataType.ONE_RM -> String.format("%.1f", calculate1RM(sets.last().weight, sets.last().reps))
                            }
                            Text(
                                "$latestValue ${if(type == DataType.REPS) "" else "kg"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        AdvancedEvolutionChart(sets, type)
                    }
                }
            }
        }
        
        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun AdvancedEvolutionChart(logs: List<ExerciseSet>, type: DataType) {
    val color = MaterialTheme.colorScheme.secondary
    val values = logs.map { 
        when(type) {
            DataType.CARGA -> it.weight.toFloatOrNull() ?: 0f
            DataType.REPS -> it.reps.toFloatOrNull() ?: 0f
            DataType.ONE_RM -> calculate1RM(it.weight, it.reps).toFloat()
        }
    }
    
    if (values.isEmpty()) return

    val maxVal = (values.maxOrNull() ?: 1f) * 1.1f
    val minVal = (values.minOrNull() ?: 0f) * 0.9f
    val range = (maxVal - minVal).coerceAtLeast(1f)

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacing = if (values.size > 1) width / (values.size - 1) else width
        
        // Draw grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height - (i.toFloat() / gridLines * height)
            drawLine(
                color = color.copy(alpha = 0.05f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val points = values.mapIndexed { i, v ->
            Offset(i * spacing, height - ((v - minVal) / range * height))
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            if (points.size > 1) {
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    cubicTo(
                        (prev.x + curr.x) / 2, prev.y,
                        (prev.x + curr.x) / 2, curr.y,
                        curr.x, curr.y
                    )
                }
            }
        }

        // Area under path
        val fillPath = Path().apply {
            addPath(path)
            if (points.size > 1) {
                lineTo(points.last().x, height)
                lineTo(points.first().x, height)
                close()
            }
        }
        
        drawPath(
            path = fillPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.2f), Color.Transparent)
            )
        )

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        points.forEachIndexed { index, point ->
            if (index == 0 || index == points.size - 1 || points.size < 10) {
                drawCircle(
                    color = color,
                    radius = 6.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }
    }
}
