package com.example.myapplication.utils

import com.example.myapplication.data.Exercise
import com.example.myapplication.data.ExerciseSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class AIInsight(
    val title: String,
    val description: String,
    val riskLevel: RiskLevel,
    val suggestion: String,
    val isComparison: Boolean = false
)

enum class RiskLevel { LOW, MEDIUM, HIGH, INSIGHT }

object AICoach {
    fun analyzeTrainingPatterns(exercises: List<Exercise>, allSets: List<ExerciseSet>): List<AIInsight> {
        val insights = mutableListOf<AIInsight>()
        if (allSets.isEmpty()) return insights

        val setsByDate = allSets.groupBy { it.date }
        val sortedDates = setsByDate.keys.toList().sortedBy { it } // Oldest to newest

        // 1. Overtraining Detection
        if (sortedDates.size >= 7) {
            val dates = sortedDates.reversed()
            val recentVolume = calculateVolume(setsByDate[dates[0]] ?: emptyList())
            val avgVolume = dates.drop(1).take(5).map { calculateVolume(setsByDate[it] ?: emptyList()) }.average()
            
            if (recentVolume > avgVolume * 1.5) {
                insights.add(AIInsight(
                    "Pico de Volume Detectado",
                    "Seu volume hoje foi 50% superior à sua média.",
                    RiskLevel.MEDIUM,
                    "Sugestão: Reduza 1 série em cada exercício no próximo treino para evitar fadiga central."
                ))
            }
        }

        // 2. IA de Comparação Histórica
        if (sortedDates.size >= 14) {
            val dailyVolumes = sortedDates.map { calculateVolume(setsByDate[it] ?: emptyList()) }
            val maxVolume = dailyVolumes.maxOrNull() ?: 0.0
            val minVolume = dailyVolumes.minOrNull() ?: 0.0
            val avgVolume = dailyVolumes.average()

            // Best Phase (Peak Performance)
            val peakDate = sortedDates[dailyVolumes.indexOf(maxVolume)]
            insights.add(AIInsight(
                "Melhor Fase Histórica",
                "Seu ápice de força foi em $peakDate com ${maxVolume.toInt()}kg de volume total.",
                RiskLevel.INSIGHT,
                "Analise o que você estava comendo e como dormia nessa época.",
                true
            ))

            // Efficient Phase (Best quality per volume)
            val qualityByDay = sortedDates.map { date ->
                val daySets = setsByDate[date] ?: emptyList()
                daySets.map { it.quality }.average()
            }
            val bestQualityDate = sortedDates[qualityByDay.indexOf(qualityByDay.maxOrNull() ?: 0.0)]
            insights.add(AIInsight(
                "Fase Mais Eficiente",
                "Em $bestQualityDate, você teve a melhor qualidade técnica média.",
                RiskLevel.INSIGHT,
                "Lembre-se: qualidade supera carga para hipertrofia a longo prazo.",
                true
            ))
            
            // Worst Phase (Drop in engagement or volume)
            if (minVolume < avgVolume * 0.4) {
                val worstDate = sortedDates[dailyVolumes.indexOf(minVolume)]
                insights.add(AIInsight(
                    "Ponto de Menor Estímulo",
                    "Em $worstDate seu volume caiu drasticamente.",
                    RiskLevel.LOW,
                    "Identifique o motivo do desânimo para não repetir o padrão.",
                    true
                ))
            }
        }

        // 3. Recovery Analysis
        val musclesTrainedRecently = mutableMapOf<String, Int>()
        sortedDates.takeLast(3).forEach { date ->
            setsByDate[date]?.forEach { set ->
                val ex = exercises.find { it.id == set.exerciseId }
                ex?.category?.let { musclesTrainedRecently[it] = (musclesTrainedRecently[it] ?: 0) + 1 }
            }
        }
        
        musclesTrainedRecently.forEach { (muscle, count) ->
            if (count > 25) {
                insights.add(AIInsight(
                    "Risco de Lesão: $muscle",
                    "Volume excessivo acumulado em $muscle nas últimas 72h.",
                    RiskLevel.HIGH,
                    "Sugestão: Dê 48h de descanso total para este grupamento."
                ))
            }
        }

        // 4. Stagnation Prediction
        exercises.forEach { ex ->
            val exSets = allSets.filter { it.exerciseId == ex.id }
            val stagnation = detectStagnation(exSets)
            if (stagnation is StagnationStatus.Stagnated) {
                insights.add(AIInsight(
                    "Platô Iminente: ${ex.name}",
                    "Seu progresso travou. O sistema nervoso não está mais respondendo a este estímulo.",
                    RiskLevel.MEDIUM,
                    stagnation.suggestion
                ))
            }
        }

        return insights
    }

    private fun calculateVolume(sets: List<ExerciseSet>): Double {
        return sets.sumOf { (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toDoubleOrNull() ?: 0.0) }
    }
}
