package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GymViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).exerciseDao()
    val allExercises: Flow<List<Exercise>> = dao.getAllExercises()
    val allCycles: Flow<List<TrainingCycle>> = dao.getAllCycles()
    val allBattleScars: Flow<List<BattleScar>> = dao.getAllBattleScars()

    fun addExercise(name: String, target: String, setsDescription: String, dayOfWeek: String, imageUri: String?) {
        viewModelScope.launch {
            val activeCycle = dao.getActiveCycle()
            dao.insertExercise(
                Exercise(
                    name = name,
                    target = target,
                    setsDescription = setsDescription,
                    dayOfWeek = dayOfWeek,
                    imageUri = imageUri,
                    cycleId = activeCycle?.id ?: 0,
                    category = guessCategory(target)
                )
            )
        }
    }

    private fun guessCategory(target: String): String {
        val t = target.lowercase()
        return when {
            t.contains("peito") || t.contains("peitoral") -> "Peito"
            t.contains("costas") || t.contains("dorsal") -> "Costas"
            t.contains("perna") || t.contains("coxa") || t.contains("quadríceps") || t.contains("glúteo") -> "Pernas"
            t.contains("ombro") || t.contains("deltoide") -> "Ombros"
            t.contains("bíceps") || t.contains("tríceps") || t.contains("braço") -> "Braços"
            t.contains("abd") || t.contains("core") -> "Core"
            else -> "Geral"
        }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch { dao.updateExercise(exercise) }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch { dao.deleteExercise(exercise) }
    }

    fun addSet(exerciseId: Long, weight: String, reps: String, technique: String? = null, quality: Int = 5) {
        viewModelScope.launch {
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM"))
            val activeCycle = dao.getActiveCycle()
            dao.insertSet(
                ExerciseSet(
                    exerciseId = exerciseId, weight = weight, reps = reps,
                    date = today, cycleId = activeCycle?.id ?: 0,
                    technique = technique, quality = quality
                )
            )
        }
    }

    fun getSets(exerciseId: Long): Flow<List<ExerciseSet>> = dao.getSetsForExercise(exerciseId)

    fun deleteSet(set: ExerciseSet) {
        viewModelScope.launch { dao.deleteSet(set) }
    }

    fun startNewCycle(name: String) {
        viewModelScope.launch {
            val active = dao.getActiveCycle()
            if (active != null) {
                dao.updateCycle(active.copy(isActive = false, endDate = LocalDate.now().toString()))
            }
            dao.insertCycle(TrainingCycle(name = name, startDate = LocalDate.now().toString(), isActive = true))
        }
    }

    fun addBattleScar(uri: String, caption: String?) {
        viewModelScope.launch {
            dao.insertBattleScar(BattleScar(imageUri = uri, date = LocalDate.now().toString(), caption = caption))
        }
    }

    fun deleteBattleScar(scar: BattleScar) {
        viewModelScope.launch { dao.deleteBattleScar(scar) }
    }

    fun importDefaultExercises() {
        viewModelScope.launch {
            val defaults = listOf(
                Exercise(name = "Supino Reto", target = "Peito", setsDescription = "4x8-12", dayOfWeek = "SEGUNDA-FEIRA", category = "Peito"),
                Exercise(name = "Agachamento", target = "Pernas", setsDescription = "4x6-10", dayOfWeek = "TERÇA-FEIRA", category = "Pernas"),
                Exercise(name = "Levantamento Terra", target = "Costas", setsDescription = "3x5", dayOfWeek = "QUARTA-FEIRA", category = "Costas"),
                Exercise(name = "Desenvolvimento", target = "Ombros", setsDescription = "4x8-12", dayOfWeek = "QUINTA-FEIRA", category = "Ombros"),
                Exercise(name = "Remada Curvada", target = "Costas", setsDescription = "4x8-12", dayOfWeek = "SEXTA-FEIRA", category = "Costas")
            )
            defaults.forEach { dao.insertExercise(it) }
        }
    }
}
