package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Exercise
import com.example.myapplication.data.ExerciseSet
import com.example.myapplication.ui.components.ReviewCard
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.utils.*
import com.example.myapplication.viewmodel.GymViewModel
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.*

@Composable
fun ReviewHubScreen(exercises: List<Exercise>, vm: GymViewModel) {
    var view by remember { mutableStateOf("HUB") }

    when (view) {
        "VOLUME" -> VolumeAnalysisDetail(exercises, vm) { view = "HUB" }
        "PERIOD" -> PeriodizationScreen(exercises, vm) { view = "HUB" }
        "MUSCLE" -> MuscleAnalyticsScreen(exercises, vm) { view = "HUB" }
        "COMPARISON" -> TimeComparisonScreen(exercises, vm) { view = "HUB" }
        "RADAR" -> PowerRadarScreen(exercises, vm) { view = "HUB" }
        else -> {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                SectionHeader(
                    title = "Análise Avançada",
                    subtitle = "O app aprende sua evolução"
                )
                
                Spacer(Modifier.height(16.dp))
                
                ReviewCard(
                    title = "Radar de Poder",
                    sub = "Atributos de combate e performance",
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color.Black,
                    icon = Icons.Rounded.Radar
                ) { view = "RADAR" }

                Spacer(Modifier.height(16.dp))
                
                ReviewCard(
                    title = "Heatmap Muscular",
                    sub = "Intensidade e transformações",
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = Icons.Rounded.Hub
                ) { view = "MUSCLE" }
                
                Spacer(Modifier.height(16.dp))

                ReviewCard(
                    title = "Comparação de Meses",
                    sub = "Este mês vs Mês passado",
                    containerColor = Color(0xFF673AB7),
                    contentColor = Color.White,
                    icon = Icons.Rounded.Timeline
                ) { view = "COMPARISON" }
                
                Spacer(Modifier.height(16.dp))
                
                ReviewCard(
                    title = "Volume por Exercício",
                    sub = "Tonelagem acumulada",
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    icon = Icons.Rounded.BarChart
                ) { view = "VOLUME" }
            }
        }
    }
}

@Composable
fun PowerRadarScreen(exercises: List<Exercise>, vm: GymViewModel, onBack: () -> Unit) {
    var allSets by remember { mutableStateOf<List<ExerciseSet>>(emptyList()) }
    LaunchedEffect(exercises) {
        val sets = mutableListOf<ExerciseSet>()
        exercises.forEach { ex -> sets.addAll(vm.getSets(ex.id).first()) }
        allSets = sets
    }

    val strength = remember(allSets) {
        if (allSets.isEmpty()) 0.2f else {
            val max1RM = allSets.maxOf { calculate1RM(it.weight, it.reps) }
            (max1RM / 200.0).coerceAtMost(1.0).toFloat().coerceAtLeast(0.1f)
        }
    }

    val endurance = remember(allSets) {
        val totalReps = allSets.sumOf { it.reps.toIntOrNull() ?: 0 }
        (totalReps / 5000.0).coerceAtMost(1.0).toFloat().coerceAtLeast(0.1f)
    }

    val consistency = remember(allSets) {
        val uniqueDays = allSets.map { it.date }.distinct().size
        (uniqueDays / 30.0).coerceAtMost(1.0).toFloat().coerceAtLeast(0.1f)
    }

    val explosiveness = remember(allSets) {
        if (allSets.size < 10) 0.3f else {
            val weights = allSets.groupBy { it.date }.mapValues { e -> e.value.maxOf { it.weight.toDoubleOrNull() ?: 0.0 } }.values.toList()
            if (weights.size < 2) 0.3f else {
                val gain = weights.last() - weights.first()
                (gain / 50.0).coerceIn(0.1, 1.0).toFloat()
            }
        }
    }

    val recovery = remember(allSets) {
        val avgQuality = if (allSets.isEmpty()) 0.5f else allSets.map { it.quality }.average().toFloat() / 5f
        avgQuality.coerceAtLeast(0.1f)
    }

    val attributes = listOf(strength, endurance, consistency, explosiveness, recovery)
    val labels = listOf("FORÇA", "RESISTÊNCIA", "CONSISTÊNCIA", "EXPLOSÃO", "RECUPERAÇÃO")

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.padding(16.dp).statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
            }
            Spacer(Modifier.width(16.dp))
            Text("Radar de Poder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Text("NÍVEL DE COMBATE ATUAL", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
                Spacer(Modifier.height(24.dp))
                PowerRadarChart(attributes, labels)
                Spacer(Modifier.height(32.dp))
            }

            items(labels.zip(attributes)) { (label, value) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${(value * 100).toInt()}%", fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
                            LinearProgressIndicator(
                                progress = { value },
                                modifier = Modifier.width(100.dp).height(4.dp).clip(CircleShape),
                                color = Color(0xFFFFD700),
                                trackColor = Color.Gray.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PowerRadarChart(values: List<Float>, labels: List<String>) {
    val radarColor = Color(0xFFFFD700)
    
    Canvas(modifier = Modifier.size(280.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2.5f
        val numAxes = values.size
        val angleStep = 2 * PI / numAxes

        for (i in 1..5) {
            val stepRadius = radius * (i / 5f)
            val path = Path()
            for (j in 0 until numAxes) {
                val angle = j * angleStep - PI / 2
                val x = center.x + stepRadius * cos(angle).toFloat()
                val y = center.y + stepRadius * sin(angle).toFloat()
                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, Color.Gray.copy(alpha = 0.2f), style = Stroke(1.dp.toPx()))
        }

        for (i in 0 until numAxes) {
            val angle = i * angleStep - PI / 2
            val end = Offset(
                center.x + radius * cos(angle).toFloat(),
                center.y + radius * sin(angle).toFloat()
            )
            drawLine(Color.Gray.copy(alpha = 0.3f), center, end, strokeWidth = 1.dp.toPx())
        }

        val powerPath = Path()
        for (i in 0 until numAxes) {
            val angle = i * angleStep - PI / 2
            val valRadius = radius * values[i]
            val x = center.x + valRadius * cos(angle).toFloat()
            val y = center.y + valRadius * sin(angle).toFloat()
            if (i == 0) powerPath.moveTo(x, y) else powerPath.lineTo(x, y)
        }
        powerPath.close()

        drawPath(
            path = powerPath,
            brush = Brush.radialGradient(
                colors = listOf(radarColor.copy(alpha = 0.6f), radarColor.copy(alpha = 0.1f))
            )
        )
        drawPath(
            path = powerPath,
            color = radarColor,
            style = Stroke(3.dp.toPx())
        )

        for (i in 0 until numAxes) {
            val angle = i * angleStep - PI / 2
            val valRadius = radius * values[i]
            drawCircle(
                color = radarColor,
                radius = 4.dp.toPx(),
                center = Offset(
                    center.x + valRadius * cos(angle).toFloat(),
                    center.y + valRadius * sin(angle).toFloat()
                )
            )
        }
    }
}

@Composable
fun MuscleAnalyticsScreen(exercises: List<Exercise>, vm: GymViewModel, onBack: () -> Unit) {
    var allSets by remember { mutableStateOf<List<ExerciseSet>>(emptyList()) }
    
    LaunchedEffect(exercises) {
        val sets = mutableListOf<ExerciseSet>()
        exercises.forEach { ex ->
            sets.addAll(vm.getSets(ex.id).first())
        }
        allSets = sets
    }
    
    val analytics = remember(exercises, allSets) { calculateMuscleAnalytics(exercises, allSets) }
    val transformation = remember(allSets) { calculateTransformation(allSets) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.padding(16.dp).statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
            }
            Spacer(Modifier.width(16.dp))
            Text("Estágio de Evolução", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.3f))
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = transformation.label.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            color = when(transformation) {
                                ZenkaiTransformation.ERVA_DANINHA -> Color.Gray
                                ZenkaiTransformation.PROJETO_DE_FRANGO -> Color.Cyan
                                ZenkaiTransformation.MUTANTE_REJEITADO -> Color.Yellow
                                ZenkaiTransformation.ABERRACAO_DIVINA -> Color.Red
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = transformation.humor,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                            AnimeHeatmapCharacter(analytics, transformation)
                        }
                    }
                }
            }

            item {
                Text("Distribuição de Fadiga", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    analytics.forEach { muscle ->
                        MuscleFatigueBar(muscle)
                    }
                }
            }

            items(analytics) { muscle ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text(muscle.muscle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                            Surface(
                                color = Color.Red.copy(alpha = 0.1f + (muscle.heatIntensity * 0.4f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "INTENSIDADE: ${(muscle.heatIntensity * 100).toInt()}%",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                            }
                        }
                        
                        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Column {
                                Text("SÉRIES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${muscle.totalSets}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("REPS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${muscle.totalReps}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("CARGA MÁX", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${muscle.maxWeight.toInt()}kg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MuscleFatigueBar(muscle: MuscleStimulus) {
    val color = when {
        muscle.fatigueLevel > 0.7f -> Color.Red
        muscle.fatigueLevel > 0.3f -> Color.Yellow
        else -> Color.Green
    }
    val animatedFatigue by animateFloatAsState(targetValue = muscle.fatigueLevel)
    
    Column {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(muscle.muscle, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(
                if(muscle.fatigueLevel > 0.1f) "DESCANSO NECESSÁRIO" else "PRONTO PARA TREINO",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontSize = 9.sp
            )
        }
        LinearProgressIndicator(
            progress = { animatedFatigue },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = color,
            trackColor = Color.Gray.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun AnimeHeatmapCharacter(analytics: List<MuscleStimulus>, transformation: ZenkaiTransformation) {
    val infiniteTransition = rememberInfiniteTransition()
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier.size(200.dp, 300.dp)) {
        val centerX = size.width / 2
        
        val peitoHeat = analytics.find { it.muscle == "Peito" }?.heatIntensity ?: 0f
        val costasHeat = analytics.find { it.muscle == "Costas" }?.heatIntensity ?: 0f
        val pernasHeat = analytics.find { it.muscle == "Pernas" }?.heatIntensity ?: 0f
        val ombrosHeat = analytics.find { it.muscle == "Ombros" }?.heatIntensity ?: 0f
        val bracosHeat = analytics.find { it.muscle == "Braços" }?.heatIntensity ?: 0f

        fun getHeatColor(intensity: Float) = Color(
            red = 0.2f + (intensity * 0.8f),
            green = 0.1f * (1 - intensity),
            blue = 0.1f * (1 - intensity),
            alpha = 0.4f + (intensity * 0.6f)
        )

        val auraColor = when(transformation) {
            ZenkaiTransformation.ERVA_DANINHA -> Color.Gray
            ZenkaiTransformation.PROJETO_DE_FRANGO -> Color.Cyan
            ZenkaiTransformation.MUTANTE_REJEITADO -> Color.Yellow
            ZenkaiTransformation.ABERRACAO_DIVINA -> Color.Red
        }

        // Aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(auraColor.copy(alpha = auraAlpha), Color.Transparent),
                center = Offset(centerX, 150.dp.toPx()),
                radius = 160.dp.toPx()
            ),
            radius = 160.dp.toPx(),
            center = Offset(centerX, 150.dp.toPx())
        )

        // DRAW LIGHTNING FOR MUTANTE REJEITADO +
        if (transformation == ZenkaiTransformation.MUTANTE_REJEITADO || transformation == ZenkaiTransformation.ABERRACAO_DIVINA) {
            for (i in 0..3) {
                val startX = centerX + ((-50..50).random().dp.toPx())
                val startY = (0..300).random().dp.toPx()
                drawLine(
                    color = if(transformation == ZenkaiTransformation.ABERRACAO_DIVINA) Color.Magenta else Color.Cyan,
                    start = Offset(startX, startY),
                    end = Offset(startX + 10.dp.toPx(), startY + 20.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Head / Mask
        val headPath = Path().apply {
            moveTo(centerX - 15.dp.toPx(), 10.dp.toPx())
            lineTo(centerX + 15.dp.toPx(), 10.dp.toPx())
            lineTo(centerX + 12.dp.toPx(), 45.dp.toPx())
            lineTo(centerX, 55.dp.toPx())
            lineTo(centerX - 12.dp.toPx(), 45.dp.toPx())
            close()
        }
        drawPath(headPath, Color.DarkGray)

        val chestPath = Path().apply {
            moveTo(centerX - 45.dp.toPx(), 60.dp.toPx())
            lineTo(centerX + 45.dp.toPx(), 60.dp.toPx())
            lineTo(centerX + 30.dp.toPx(), 140.dp.toPx())
            lineTo(centerX - 30.dp.toPx(), 140.dp.toPx())
            close()
        }
        drawPath(chestPath, getHeatColor((peitoHeat + costasHeat)/2))

        drawCircle(getHeatColor(ombrosHeat), 22.dp.toPx(), center = Offset(centerX - 50.dp.toPx(), 75.dp.toPx()))
        drawCircle(getHeatColor(ombrosHeat), 22.dp.toPx(), center = Offset(centerX + 50.dp.toPx(), 75.dp.toPx()))

        val leftArm = Path().apply {
            moveTo(centerX - 65.dp.toPx(), 90.dp.toPx())
            quadraticTo(centerX - 85.dp.toPx(), 130.dp.toPx(), centerX - 60.dp.toPx(), 170.dp.toPx())
            lineTo(centerX - 45.dp.toPx(), 160.dp.toPx())
            close()
        }
        val rightArm = Path().apply {
            moveTo(centerX + 65.dp.toPx(), 90.dp.toPx())
            quadraticTo(centerX + 85.dp.toPx(), 130.dp.toPx(), centerX + 60.dp.toPx(), 170.dp.toPx())
            lineTo(centerX + 45.dp.toPx(), 160.dp.toPx())
            close()
        }
        drawPath(leftArm, getHeatColor(bracosHeat))
        drawPath(rightArm, getHeatColor(bracosHeat))

        val leftLeg = Path().apply {
            moveTo(centerX - 30.dp.toPx(), 145.dp.toPx())
            lineTo(centerX - 10.dp.toPx(), 145.dp.toPx())
            lineTo(centerX - 15.dp.toPx(), 260.dp.toPx())
            lineTo(centerX - 40.dp.toPx(), 260.dp.toPx())
            close()
        }
        val rightLeg = Path().apply {
            moveTo(centerX + 30.dp.toPx(), 145.dp.toPx())
            lineTo(centerX + 10.dp.toPx(), 145.dp.toPx())
            lineTo(centerX + 15.dp.toPx(), 260.dp.toPx())
            lineTo(centerX + 40.dp.toPx(), 260.dp.toPx())
            close()
        }
        drawPath(leftLeg, getHeatColor(pernasHeat))
        drawPath(rightLeg, getHeatColor(pernasHeat))
    }
}

@Composable
fun TimeComparisonScreen(exercises: List<Exercise>, vm: GymViewModel, onBack: () -> Unit) {
    var allSets by remember { mutableStateOf<List<ExerciseSet>>(emptyList()) }
    LaunchedEffect(exercises) {
        val sets = mutableListOf<ExerciseSet>()
        exercises.forEach { ex -> sets.addAll(vm.getSets(ex.id).first()) }
        allSets = sets
    }

    val now = LocalDate.now()
    val thisMonthSets = allSets.filter { 
        try {
            val date = LocalDate.parse("${it.date}/${now.year}", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            date.month == now.month && date.year == now.year
        } catch(e: Exception) { false }
    }
    val lastMonthSets = allSets.filter { 
        try {
            val date = LocalDate.parse("${it.date}/${now.year}", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            date.month == now.minusMonths(1).month && date.year == now.minusMonths(1).year
        } catch(e: Exception) { false }
    }

    val thisMonthVol = thisMonthSets.sumOf { (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toDoubleOrNull() ?: 0.0) }
    val lastMonthVol = lastMonthSets.sumOf { (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toDoubleOrNull() ?: 0.0) }
    val diff = if (lastMonthVol > 0) ((thisMonthVol - lastMonthVol) / lastMonthVol) * 100 else 0.0

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.padding(16.dp).statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
            }
            Spacer(Modifier.width(16.dp))
            Text("Oráculo de Poder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Column(Modifier.padding(24.dp)) {
                        Text("Volume Mensal", color = Color.White.copy(alpha = 0.7f))
                        Text("${thisMonthVol.toInt()} kg", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = Color.White)
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (diff >= 0) Icons.Rounded.AutoGraph else Icons.AutoMirrored.Rounded.TrendingDown, null, tint = if(diff >= 0) Color(0xFF00E676) else Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                String.format(Locale.getDefault(), "${if(diff >= 0) "+" else ""}${diff.toInt()}%% evolução"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text("PRÓXIMO DESPERTAR (PREVISÃO)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(exercises) { ex ->
                val exSets = allSets.filter { it.exerciseId == ex.id }
                val prediction = predictStrength(ex.name, exSets)
                if (prediction != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.AutoAwesome, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(ex.name, fontWeight = FontWeight.Black)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Você atingirá ${prediction.targetWeight.toInt()}kg em aproximadamente ${prediction.weeksToReach} semanas.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("Sincronização com o Peso", style = MaterialTheme.typography.labelSmall, color = Color.Cyan)
                            LinearProgressIndicator(
                                progress = { prediction.syncPercentage },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = Color.Cyan,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VolumeAnalysisDetail(exercises: List<Exercise>, vm: GymViewModel, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "Volume Acumulado",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exercises) { ex ->
                val sets by vm.getSets(ex.id).collectAsState(initial = emptyList())
                val volume = sets.sumOf { (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toDoubleOrNull() ?: 0.0) }
                
                if (volume > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ex.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(
                                String.format(Locale.getDefault(), "%.0f kg", volume),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun PeriodizationScreen(exercises: List<Exercise>, vm: GymViewModel, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "Sugestões de Progressão",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exercises) { ex ->
                val sets by vm.getSets(ex.id).collectAsState(initial = emptyList())
                val status = getProgressionStatus(ex.setsDescription, sets)
                
                if (status is ProgressionResult.Apt) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                ex.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Meta batida! Sugestão de aumento de carga:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                String.format(Locale.getDefault(), "+3%% (%.1f kg)", status.lastWeight * 1.03),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}
