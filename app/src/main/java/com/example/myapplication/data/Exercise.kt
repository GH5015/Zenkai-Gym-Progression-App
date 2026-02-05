package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val target: String,
    val category: String = "Geral", // Peito, Costas, etc.
    val setsDescription: String,
    val dayOfWeek: String,
    val imageUri: String? = null,
    val cycleId: Long = 0
)

@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExerciseSet(
    @PrimaryKey(autoGenerate = true) val setId: Long = 0,
    val exerciseId: Long,
    val weight: String,
    val reps: String,
    val date: String,
    val cycleId: Long = 0,
    val technique: String? = null, // Rest-pause, Drop set, etc.
    val quality: Int = 5 // 1-5 scale for technique control
)

@Entity(tableName = "training_cycles")
data class TrainingCycle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startDate: String,
    val endDate: String? = null,
    val isActive: Boolean = true,
    val note: String? = null
)
