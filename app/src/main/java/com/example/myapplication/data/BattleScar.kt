package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battle_scars")
data class BattleScar(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUri: String,
    val date: String,
    val caption: String? = null
)
