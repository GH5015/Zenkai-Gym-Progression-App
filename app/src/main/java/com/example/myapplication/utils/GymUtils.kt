package com.example.myapplication.utils

import com.example.myapplication.data.ExerciseSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

fun calculate1RM(weight: String, reps: String): Double {
    val w = weight.toDoubleOrNull() ?: 0.0
    val r = reps.toDoubleOrNull() ?: 0.0
    return if (r > 0) w * (1 + r / 30.0) else w
}

fun getTargetSets(setsDescription: String): Int {
    val regex = Regex("""^(\d+)\s*[xX]""")
    return regex.find(setsDescription)?.groupValues?.get(1)?.toIntOrNull() ?: 0
}

fun getTargetCeiling(setsDescription: String): Int {
    return setsDescription.split("-").lastOrNull()?.filter { it.isDigit() }?.toIntOrNull()
        ?: setsDescription.split("x", ignoreCase = true).lastOrNull()?.filter { it.isDigit() }?.toIntOrNull() ?: 0
}

sealed class ProgressionResult {
    object None : ProgressionResult()
    object Consolidating : ProgressionResult()
    data class Apt(val lastWeight: Double) : ProgressionResult()
}

fun getProgressionStatus(setsDescription: String, lastSets: List<ExerciseSet>): ProgressionResult {
    if (lastSets.isEmpty()) return ProgressionResult.None
    val ceiling = getTargetCeiling(setsDescription)
    if (ceiling == 0) return ProgressionResult.None
    val lastDate = lastSets.last().date
    val lastWorkout = lastSets.filter { it.date == lastDate }
    
    val targetSets = getTargetSets(setsDescription)
    if (lastWorkout.size < targetSets) return ProgressionResult.Consolidating
    
    val allHitCeiling = lastWorkout.all { (it.reps.toIntOrNull() ?: 0) >= ceiling }
    return if (allHitCeiling) ProgressionResult.Apt(lastWorkout.last().weight.toDoubleOrNull() ?: 0.0)
    else ProgressionResult.Consolidating
}

sealed class StagnationStatus {
    object Progressing : StagnationStatus()
    data class Stagnated(val weeks: Int, val suggestion: String) : StagnationStatus()
    data class FormDeviated(val warning: String) : StagnationStatus()
}

fun detectStagnation(sets: List<ExerciseSet>): StagnationStatus {
    if (sets.size < 10) return StagnationStatus.Progressing
    
    val workouts = sets.groupBy { it.date }
        .mapValues { entry -> 
            val maxW = entry.value.maxOf { it.weight.toDoubleOrNull() ?: 0.0 }
            val avgQuality = entry.value.map { it.quality }.average()
            val totalReps = entry.value.sumOf { it.reps.toIntOrNull() ?: 0 }
            Triple(maxW, avgQuality, totalReps)
        }
        .toList()
        .sortedByDescending { it.first }
        .take(4)

    if (workouts.size < 3) return StagnationStatus.Progressing

    val current = workouts[0].second
    val prev = workouts[1].second
    
    if (current.first > prev.first && current.third < prev.third && current.second < 4.0) {
        return StagnationStatus.FormDeviated("Alerta: Aumento de carga com perda de reps e controle técnico.")
    }

    val weights = workouts.map { it.second.first }
    val isStagnated = weights[0] <= weights[1] && weights[1] <= weights[2]
    
    return if (isStagnated) {
        StagnationStatus.Stagnated(
            weeks = 2,
            suggestion = "Estagnação detectada: Tente um deload de 10% ou mude a técnica (ex: Rest-pause)."
        )
    } else {
        StagnationStatus.Progressing
    }
}

data class MuscleStimulus(
    val muscle: String,
    val maxWeight: Double,
    val weeklyVolume: Double,
    val totalSets: Int,
    val totalReps: Int,
    val growthPhase: String,
    val heatIntensity: Float, // 0.0 to 1.0
    val fatigueLevel: Float // 0.0 (fresh) to 1.0 (exhausted)
)

fun calculateMuscleAnalytics(allExercises: List<com.example.myapplication.data.Exercise>, allSets: List<ExerciseSet>): List<MuscleStimulus> {
    val muscles = listOf("Peito", "Costas", "Pernas", "Ombros", "Braços")
    val now = LocalDate.now()
    
    val analytics = muscles.map { muscle ->
        val relevantExercises = allExercises.filter { it.category == muscle || it.target.contains(muscle, ignoreCase = true) }
        val relevantSets = allSets.filter { set -> relevantExercises.any { it.id == set.exerciseId } }
        
        val maxW = relevantSets.maxOfOrNull { it.weight.toDoubleOrNull() ?: 0.0 } ?: 0.0
        val vol = relevantSets.sumOf { (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toDoubleOrNull() ?: 0.0) }
        val totalSets = relevantSets.size
        val totalReps = relevantSets.sumOf { it.reps.toIntOrNull() ?: 0 }
        
        // Fatigue logic
        val lastSet = relevantSets.lastOrNull()
        val fatigue = if (lastSet != null) {
            try {
                val lastDate = LocalDate.parse("${lastSet.date}/${now.year}", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                val daysPassed = abs(ChronoUnit.DAYS.between(lastDate, now))
                (1.0f - (daysPassed.toFloat() / 3.0f)).coerceIn(0.0f, 1.0f) // Recovers fully in 3 days
            } catch (e: Exception) { 0.0f }
        } else 0.0f
        
        MuscleStimulus(
            muscle = muscle,
            maxWeight = maxW,
            weeklyVolume = vol / 4.0,
            totalSets = totalSets,
            totalReps = totalReps,
            growthPhase = if (vol > 1000) "Crescimento" else "Estabilizado",
            heatIntensity = (vol / 5000.0).coerceAtMost(1.0).toFloat(),
            fatigueLevel = fatigue
        )
    }
    return analytics
}

enum class ZenkaiTransformation(val label: String, val description: String, val humor: String) {
    ERVA_DANINHA(
        "ERVA DANINHA",
        "Você mal respira e sua única atividade física é mastigar.",
        "Sua existência é um erro estatístico para a evolução. Se você parar de se mexer, os urubus começam a circular."
    ),
    PROJETO_DE_FRANGO(
        "PROJETO DE Frango",
        "Você começou a treinar, mas qualquer brisa mais forte te derruba.",
        "Parabéns, você agora é um esqueleto que carrega sacolas. Ainda é um lixo, mas pelo menos é um lixo que se move."
    ),
    MUTANTE_REJEITADO(
        "MUTANTE REJEITADO",
        "Você já tem força, mas sua aparência ainda é questionável e seu psicológico está quebrado.",
        "As pessoas atravessam a rua quando te veem. Você não é humano, mas também não é um deus. É apenas alguém que trocou a alma por bíceps."
    ),
    ABERRACAO_DIVINA(
        "ABERRAÇÃO DIVINA",
        "O ápice. Você não treina, você pune o ferro.",
        "As leis da física são apenas sugestões para você. O inferno está cheio, então você resolveu ficar na Terra humilhando os mortais."
    )
}

fun calculateTransformation(allSets: List<ExerciseSet>): ZenkaiTransformation {
    if (allSets.isEmpty()) return ZenkaiTransformation.ERVA_DANINHA
    
    val totalVolume = allSets.sumOf { (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toDoubleOrNull() ?: 0.0) }
    val maxWeight = allSets.maxOfOrNull { it.weight.toDoubleOrNull() ?: 0.0 } ?: 0.0
    
    return when {
        totalVolume > 20000 || maxWeight > 180 -> ZenkaiTransformation.ABERRACAO_DIVINA
        totalVolume > 10000 -> ZenkaiTransformation.MUTANTE_REJEITADO
        totalVolume > 5000 -> ZenkaiTransformation.PROJETO_DE_FRANGO
        else -> ZenkaiTransformation.ERVA_DANINHA
    }
}

data class StrengthPrediction(
    val exerciseName: String,
    val targetWeight: Double,
    val weeksToReach: Int,
    val confidence: String,
    val syncPercentage: Float
)

fun predictStrength(exerciseName: String, sets: List<ExerciseSet>): StrengthPrediction? {
    if (sets.size < 10) return null
    
    val workouts = sets.groupBy { it.date }
        .mapValues { entry -> entry.value.maxOf { it.weight.toDoubleOrNull() ?: 0.0 } }
        .toList()
        .sortedBy { it.first }
    
    if (workouts.size < 5) return null
    
    val weights = workouts.map { it.second }
    val currentWeight = weights.last()
    val initialWeight = weights.first()
    
    val totalGain = currentWeight - initialWeight
    if (totalGain <= 0) return null
    
    val avgGainPerWorkout = totalGain / workouts.size
    val nextMilestone = ((currentWeight / 10).toInt() + 1) * 10.0
    val weightToGain = nextMilestone - currentWeight
    val workoutsToReach = (weightToGain / avgGainPerWorkout).toInt().coerceAtLeast(1)
    
    return StrengthPrediction(
        exerciseName = exerciseName,
        targetWeight = nextMilestone,
        weeksToReach = (workoutsToReach / 2).coerceAtLeast(1),
        confidence = "Alta",
        syncPercentage = (currentWeight.toFloat() / nextMilestone.toFloat()).coerceIn(0f, 1f)
    )
}
