package com.example.myapplication.ui.screens

import android.app.NotificationManager
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.data.Exercise
import com.example.myapplication.data.ExerciseSet
import com.example.myapplication.utils.calculate1RM
import com.example.myapplication.utils.getTargetSets
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseDetailScreen(
    exercise: Exercise,
    sets: List<ExerciseSet>,
    onAddSet: (String, String, String?, Int) -> Unit,
    onDeleteSet: (ExerciseSet) -> Unit,
    onBack: () -> Unit
) {
    val lastSet = sets.lastOrNull()
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var quality by remember { mutableIntStateOf(5) }
    var selectedTechnique by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(lastSet) {
        if (weight.isEmpty() && lastSet != null) weight = lastSet.weight
        if (reps.isEmpty() && lastSet != null) reps = lastSet.reps
    }
    
    var timeLeft by remember { mutableIntStateOf(0) }
    var isAlarmActive by remember { mutableStateOf(false) }
    var initialTime by remember { mutableIntStateOf(120) }
    
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val notificationManager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM"))
    val logsToday = sets.filter { it.date == today }
    val targetSets = getTargetSets(exercise.setsDescription)
    
    // Ghost Training Logic
    val lastWorkoutDate = sets.map { it.date }.distinct().let { dates ->
        if (dates.size > 1) dates[dates.size - 2] else null
    }
    val ghostSets = if (lastWorkoutDate != null) sets.filter { it.date == lastWorkoutDate } else emptyList()
    val ghostTarget = ghostSets.size
    val currentProgress = logsToday.size
    
    var showConfetti by remember { mutableStateOf(false) }
    var showTechniqueMenu by remember { mutableStateOf(false) }

    val techniques = listOf("Normal", "Rest-pause (Yates)", "Drop set (Arnold)", "Tempo (Zane)", "Partial reps", "Isometria")

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            isAlarmActive = false
            delay(1000L)
            timeLeft -= 1
            if (timeLeft == 0) {
                isAlarmActive = true
                sendTimerNotification(context, notificationManager)
            }
        }
    }

    LaunchedEffect(isAlarmActive) {
        if (isAlarmActive) {
            val pattern = longArrayOf(0, 500, 300, 500, 300)
            try {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } catch (e: Exception) {
                vibrator?.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } else {
            vibrator?.cancel()
            notificationManager.cancel(1001)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            vibrator?.cancel()
            notificationManager.cancel(1001)
        }
    }

    val setsByWeek = remember(sets) {
        sets.groupBy { log ->
            try {
                val dateStr = if (log.date.count { it == '/' } == 1) "${log.date}/${LocalDate.now().year}" else log.date
                val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                "Semana ${date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())}"
            } catch (e: Exception) { "Histórico" }
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(48.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(exercise.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        Text(exercise.target, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                if (ghostTarget > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.DirectionsRun, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Ghost Training: Você vs Passado", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            }
                            Spacer(Modifier.height(8.dp))
                            Box(Modifier.fillMaxWidth().height(24.dp)) {
                                val ghostProgress = 1f
                                LinearProgressIndicator(progress = { ghostProgress }, modifier = Modifier.fillMaxSize().clip(CircleShape), color = Color.Gray.copy(alpha = 0.3f), trackColor = Color.Transparent)
                                val raceProgress = if (ghostTarget > 0) currentProgress.toFloat() / ghostTarget else 0f
                                val animatedRaceProgress by animateFloatAsState(raceProgress)
                                LinearProgressIndicator(progress = { animatedRaceProgress.coerceAtMost(1f) }, modifier = Modifier.fillMaxSize().clip(CircleShape), color = if (raceProgress >= 1f) Color(0xFF00E676) else MaterialTheme.colorScheme.primary, trackColor = Color.Transparent)
                                Text(text = if (currentProgress >= ghostTarget) "GANHANDO! 🔥" else "ALCANÇANDO...", modifier = Modifier.align(Alignment.Center), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, fontSize = 9.sp)
                            }
                        }
                    }
                }

                val progress = if (targetSets > 0) logsToday.size.toFloat() / targetSets else 0f
                LinearProgressIndicator(progress = { progress.coerceAtMost(1f) }, modifier = Modifier.fillMaxWidth().height(10.dp).padding(vertical = 8.dp).clip(CircleShape), color = if (progress >= 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary)
                
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)) {
                    Column(Modifier.padding(24.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Série ${logsToday.size + 1}", fontWeight = FontWeight.Black)
                            TextButton(onClick = { showTechniqueMenu = true }, colors = ButtonDefaults.buttonColors(contentColor = Color.White)) {
                                Icon(Icons.Rounded.Bolt, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(selectedTechnique ?: "Técnica", fontSize = 12.sp)
                            }
                        }
                        
                        Row(Modifier.padding(top = 8.dp), Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("kg") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), placeholder = { Text(lastSet?.weight ?: "", color = Color.White.copy(0.4f)) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = Color.White, unfocusedLabelColor = Color.White, focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(0.5f)))
                            OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("reps") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), placeholder = { Text(lastSet?.reps ?: "", color = Color.White.copy(0.4f)) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = Color.White, unfocusedLabelColor = Color.White, focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(0.5f)))
                        }
                        
                        Text("Qualidade Técnica: $quality/5", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 12.dp))
                        Slider(value = quality.toFloat(), onValueChange = { quality = it.toInt() }, valueRange = 1f..5f, steps = 3, colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White))
                        
                        Button(onClick = {
                            if (weight.isNotBlank() && reps.isNotBlank()) {
                                onAddSet(weight, reps, selectedTechnique, quality)
                                if (logsToday.size + 1 >= targetSets) showConfetti = true
                                timeLeft = initialTime; quality = 5
                            }
                        }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(16.dp)) {
                            Text("Salvar Série", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            
            item {
                AnimatedVisibility(visible = timeLeft > 0 || isAlarmActive) {
                    val containerColor = if (isAlarmActive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                    val contentColor = if (isAlarmActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                    
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = containerColor)) {
                        Row(Modifier.padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text(
                                text = if (isAlarmActive) "FIM DO DESCANSO!" else "${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = contentColor
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isAlarmActive) {
                                    IconButton(onClick = { timeLeft = (timeLeft - 30).coerceAtLeast(0) }) { Icon(Icons.Rounded.RemoveCircleOutline, null, tint = MaterialTheme.colorScheme.primary) }
                                    IconButton(onClick = { timeLeft += 30 }) { Icon(Icons.Rounded.AddCircleOutline, null, tint = MaterialTheme.colorScheme.primary) }
                                }
                                IconButton(onClick = { 
                                    timeLeft = 0
                                    isAlarmActive = false 
                                }) { 
                                    Icon(if (isAlarmActive) Icons.Rounded.NotificationsOff else Icons.Rounded.Close, null, tint = contentColor) 
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("Histórico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            setsByWeek.keys.reversed().forEach { week ->
                stickyHeader {
                    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
                        Text(week.uppercase(), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
                items(setsByWeek[week]?.reversed() ?: emptyList()) { log -> ExerciseSetItem(log, { onDeleteSet(log) }) }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }

        if (showTechniqueMenu) {
            AlertDialog(onDismissRequest = { showTechniqueMenu = false }, title = { Text("Técnicas Avançadas") }, text = {
                Column {
                    techniques.forEach { tech ->
                        ListItem(headlineContent = { Text(tech) }, modifier = Modifier.clickable { selectedTechnique = if(tech == "Normal") null else tech; showTechniqueMenu = false })
                    }
                }
            }, confirmButton = {})
        }
        
        if (showConfetti) {
            LaunchedEffect(Unit) { delay(2000L); showConfetti = false }
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
                Text("META BATIDA! ⚡", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            }
        }
    }
}

private fun sendTimerNotification(context: Context, notificationManager: NotificationManager) {
    val builder = NotificationCompat.Builder(context, "ZENKAI_TIMER")
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Use default or specific icon
        .setContentTitle("ZENKAI: Tempo Esgotado!")
        .setContentText("O descanso terminou. Hora da próxima série!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setAutoCancel(true)

    notificationManager.notify(1001, builder.build())
}

@Composable
fun ExerciseSetItem(log: ExerciseSet, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${log.weight}kg x ${log.reps}", fontWeight = FontWeight.Bold)
                    if (log.technique != null) {
                        Spacer(Modifier.width(8.dp))
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(4.dp)) {
                            Text(log.technique, modifier = Modifier.padding(horizontal = 4.dp), style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                        }
                    }
                }
                Text("Qualidade: ${log.quality}/5 • ${log.date}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
            }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error.copy(0.4f), modifier = Modifier.size(18.dp)) }
        }
    }
}
