package com.example.myapplication.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Exercise
import com.example.myapplication.data.ExerciseSet
import com.example.myapplication.data.calculateUserStats
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.viewmodel.GymViewModel
import kotlinx.coroutines.flow.first

@Composable
fun ProfileScreen(exercises: List<Exercise>, vm: GymViewModel) {
    var allSets by remember { mutableStateOf<List<ExerciseSet>>(emptyList()) }
    val cycles by vm.allCycles.collectAsState(initial = emptyList())
    
    LaunchedEffect(exercises) {
        val sets = mutableListOf<ExerciseSet>()
        exercises.forEach { ex ->
            sets.addAll(vm.getSets(ex.id).first())
        }
        allSets = sets
    }
    
    val stats = remember(exercises, allSets, cycles) { calculateUserStats(exercises, allSets, cycles) }
    
    val achievements = remember(stats) {
        listOf(
            Achievement("Recruta", "Realizou o primeiro treino", stats.totalWorkouts >= 1, Icons.Rounded.RocketLaunch),
            Achievement("Focado", "Completou 5 treinos", stats.totalWorkouts >= 5, Icons.Rounded.EmojiEvents),
            Achievement("Veterano", "Completou 20 treinos", stats.totalWorkouts >= 20, Icons.Rounded.WorkspacePremium),
            Achievement("Lenda do Ginásio", "Completou 100 treinos", stats.totalWorkouts >= 100, Icons.Rounded.AutoAwesome),
            Achievement("Força Bruta", "Levantou mais de 100kg em um exercício", stats.maxWeight >= 100, Icons.Rounded.FitnessCenter),
            Achievement("Titã", "Levantou mais de 200kg em um exercício", stats.maxWeight >= 200, Icons.Rounded.Bolt),
            Achievement("Mestre do Volume", "Acumulou 10 toneladas levantadas", stats.totalVolume >= 10000, Icons.Rounded.BarChart),
            Achievement("Hércules", "Acumulou 100 toneladas levantadas", stats.totalVolume >= 100000, Icons.Rounded.MilitaryTech),
            Achievement("Persistente", "Streak de 3 dias", stats.streak >= 3, Icons.Rounded.Whatshot),
            Achievement("Inabalável", "Streak de 7 dias", stats.streak >= 7, Icons.Rounded.LocalFireDepartment),
            Achievement("Deus do Ferro", "Streak de 30 dias", stats.streak >= 30, Icons.Rounded.AllInclusive),
            Achievement("Colecionador", "Criou 10 exercícios diferentes", stats.exerciseCount >= 10, Icons.Rounded.Category),
            Achievement("Maratonista de Séries", "Realizou mais de 500 séries", stats.totalSets >= 500, Icons.Rounded.Repeat),
            Achievement("Supremo", "Atingiu um 1RM estimado de 150kg", stats.max1RM >= 150, Icons.Rounded.Stars)
        )
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "Meu Perfil", subtitle = "Seu nível e conquistas")
        }
        
        item {
            LevelCard(stats.level, stats.currentXp, stats.nextLevelXp)
        }
        
        item {
            Text("Estatísticas Gerais", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatMiniCard(Modifier.weight(1f), "Treinos", stats.totalWorkouts.toString(), Icons.Rounded.History, Color(0xFF2196F3))
                    StatMiniCard(Modifier.weight(1f), "Sequência", "${stats.streak}d", Icons.Rounded.Whatshot, Color(0xFFFF9800))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatMiniCard(Modifier.weight(1f), "Volume", "${(stats.totalVolume / 1000).toInt()}t", Icons.Rounded.BarChart, Color(0xFF9C27B0))
                    StatMiniCard(Modifier.weight(1f), "Peso Máximo", "${stats.maxWeight.toInt()}kg", Icons.Rounded.FitnessCenter, Color(0xFFF44336))
                }
            }
        }
        
        item {
            Text("Conquistas (${achievements.count { it.isUnlocked }}/${achievements.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        items(achievements) { achievement ->
            AchievementRow(achievement.title, achievement.desc, achievement.isUnlocked, achievement.icon)
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }
}

data class Achievement(val title: String, val desc: String, val isUnlocked: Boolean, val icon: ImageVector)

@Composable
fun LevelCard(level: Int, xp: Int, nextLevelXp: Int) {
    val progress = xp.toFloat() / nextLevelXp
    val animatedProgress by animateFloatAsState(targetValue = progress)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(level.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    val title = when {
                        level < 5 -> "Novato"
                        level < 15 -> "Atleta Intermediário"
                        level < 30 -> "Guerreiro do Ferro"
                        else -> "Mestre Supremo"
                    }
                    Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.8f))
                    Text("Nível $level", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$xp / $nextLevelXp XP", style = MaterialTheme.typography.labelLarge, color = Color.White)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.6f))
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun StatMiniCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AchievementRow(title: String, desc: String, isUnlocked: Boolean, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isUnlocked) Color(0xFFFFD700).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(icon, null, modifier = Modifier.padding(14.dp), tint = if (isUnlocked) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isUnlocked) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isUnlocked) Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
        else Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
    }
}
