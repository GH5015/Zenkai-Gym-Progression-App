package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.Exercise
import com.example.myapplication.data.ExerciseSet
import com.example.myapplication.ui.components.AddExerciseDialog
import com.example.myapplication.ui.components.ImportExerciseDialog
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.utils.*
import com.example.myapplication.viewmodel.GymViewModel
import kotlinx.coroutines.flow.first

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    exercises: List<Exercise>,
    onExerciseClick: (Exercise) -> Unit,
    onDelete: (Exercise) -> Unit,
    onAdd: (String, String, String, String, String?) -> Unit,
    onUpdate: (Exercise) -> Unit,
    vm: GymViewModel
) {
    val grouped = remember(exercises) { exercises.groupBy { it.dayOfWeek } }
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var selectedDay by remember { mutableStateOf("") }
    val days = listOf("SEGUNDA-FEIRA", "TERÇA-FEIRA", "QUARTA-FEIRA", "QUINTA-FEIRA", "SEXTA-FEIRA")
    
    val allCycles by vm.allCycles.collectAsState(initial = emptyList())
    val activeCycle = allCycles.find { it.isActive }
    val cycleNumber = allCycles.size

    val quotes = remember {
        listOf(
            "O cansaço é a prova de que você não desistiu.",
            "Supere seus limites. Torne-se a lenda que você nasceu para ser.",
            "Um dia sem treino é um dia desperdiçado na busca pelo topo.",
            "A dor hoje será a força de amanhã. Não pare agora!",
            "Seu corpo é seu templo, e o ferro é seu mestre.",
            "O verdadeiro poder vem da mente. O corpo apenas obedece.",
            "A disciplina é o fogo que forja o campeão.",
            "Não olhe para trás. Sua única meta é o amanhã."
        ).shuffled()
    }
    val currentQuote = remember { quotes.first() }

    var showAIInsights by remember { mutableStateOf(false) }
    val allSets = remember { mutableStateListOf<ExerciseSet>() }
    LaunchedEffect(exercises) {
        allSets.clear()
        exercises.forEach { ex -> vm.getSets(ex.id).first().forEach { allSets.add(it) } }
    }
    val insights = remember(exercises, allSets) { AICoach.analyzeTrainingPatterns(exercises, allSets) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "ZENKAI",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = activeCycle?.name ?: "ESTADO BASE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                BadgedBox(badge = { if (insights.isNotEmpty()) Badge { Text(insights.size.toString()) } }) {
                    IconButton(
                        onClick = { showAIInsights = true },
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                ),
                                shape = CircleShape
                            )
                            .shadow(8.dp, CircleShape)
                    ) {
                        Icon(Icons.Rounded.Psychology, null, tint = Color.White)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.3f))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.FormatQuote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = currentQuote,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        if (insights.isNotEmpty()) {
            item {
                Card(
                    onClick = { showAIInsights = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.WarningAmber, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SISTEMA: ${insights.size} ANOMALIAS DETECTADAS", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        days.forEach { day ->
            stickyHeader {
                Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(4.dp, 24.dp).background(MaterialTheme.colorScheme.primary))
                            Spacer(Modifier.width(12.dp))
                            Text(day, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                        }
                        Row {
                            IconButton(onClick = { selectedDay = day; showImportDialog = true }) { Icon(Icons.Rounded.LibraryAdd, null, tint = MaterialTheme.colorScheme.tertiary) }
                            IconButton(onClick = { selectedDay = day; exerciseToEdit = null; showAddDialog = true }) { Icon(Icons.Rounded.AddBox, null, tint = MaterialTheme.colorScheme.primary) }
                        }
                    }
                }
            }

            val exercisesForDay = grouped[day] ?: emptyList()
            items(exercisesForDay, key = { it.id }) { ex ->
                val sets by vm.getSets(ex.id).collectAsState(initial = emptyList())
                ExerciseCardZenkai(ex = ex, sets = sets, onClick = { onExerciseClick(ex) }, onDelete = { onDelete(ex) }, onEdit = { exerciseToEdit = ex; selectedDay = ex.dayOfWeek; showAddDialog = true })
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }

    if (showAIInsights) {
        AlertDialog(
            onDismissRequest = { showAIInsights = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("ANÁLISE DE PODER (AI)", fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(insights) { insight ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when(insight.riskLevel) {
                                    RiskLevel.LOW -> Color.DarkGray
                                    RiskLevel.MEDIUM -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    RiskLevel.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    RiskLevel.INSIGHT -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                }
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, when(insight.riskLevel) {
                                RiskLevel.LOW -> Color.Gray
                                RiskLevel.MEDIUM -> MaterialTheme.colorScheme.secondary
                                RiskLevel.HIGH -> MaterialTheme.colorScheme.error
                                RiskLevel.INSIGHT -> MaterialTheme.colorScheme.tertiary
                            })
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(insight.title.uppercase(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                Text(insight.description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                                Text("DIRETRIZ: ${insight.suggestion}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { showAIInsights = false }, shape = RoundedCornerShape(4.dp)) { Text("CONFIRMAR") } }
        )
    }

    if (showAddDialog) {
        AddExerciseDialog(exerciseToEdit = exerciseToEdit, onDismiss = { showAddDialog = false; exerciseToEdit = null }, onConfirm = { n, t, s, i -> if (exerciseToEdit == null) onAdd(n, t, s, selectedDay, i) else onUpdate(exerciseToEdit!!.copy(name = n, target = t, setsDescription = s, imageUri = i)); showAddDialog = false; exerciseToEdit = null })
    }

    if (showImportDialog) {
        ImportExerciseDialog(onDismiss = { showImportDialog = false }, onConfirm = { name, target, sets -> onAdd(name, target, sets, selectedDay, null); showImportDialog = false })
    }
}

@Composable
fun ExerciseCardZenkai(ex: Exercise, sets: List<ExerciseSet>, onClick: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    val stagnation = remember(sets) { detectStagnation(sets) }
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.shadow(2.dp, RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))) {
                    if (ex.imageUri != null) AsyncImage(model = ex.imageUri, contentDescription = null, contentScale = ContentScale.Crop)
                    else Icon(Icons.Rounded.FitnessCenter, null, Modifier.align(Alignment.Center), tint = MaterialTheme.colorScheme.primary)
                }
                Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    Text(ex.name.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Text(ex.target.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Rounded.EditNote, null, tint = MaterialTheme.colorScheme.tertiary) }
                    IconButton(onClick = onDelete) { Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.error) }
                }
            }
            
            if (stagnation !is StagnationStatus.Progressing) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Bolt, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            val title = if(stagnation is StagnationStatus.FormDeviated) "DESVIO TÉCNICO" else "ESTAGNAÇÃO"
                            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
