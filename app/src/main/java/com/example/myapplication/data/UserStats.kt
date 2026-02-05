package com.example.myapplication.data

import com.example.myapplication.utils.calculate1RM

data class UserStats(
    val level: Int,
    val currentXp: Int,
    val nextLevelXp: Int,
    val streak: Int,
    val totalVolume: Double,
    val totalWorkouts: Int,
    val totalReps: Int,
    val totalSets: Int,
    val maxWeight: Double,
    val max1RM: Double,
    val exerciseCount: Int,
    val cycleCount: Int
)

fun calculateUserStats(exercises: List<Exercise>, allSets: List<ExerciseSet>, cycles: List<TrainingCycle>): UserStats {
    val totalVolume = allSets.sumOf { (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toDoubleOrNull() ?: 0.0) }
    val totalReps = allSets.sumOf { it.reps.toIntOrNull() ?: 0 }
    val totalSets = allSets.size
    val uniqueDates = allSets.map { it.date }.distinct().size
    val maxWeight = allSets.maxOfOrNull { it.weight.toDoubleOrNull() ?: 0.0 } ?: 0.0
    val max1RM = allSets.maxOfOrNull { calculate1RM(it.weight, it.reps) } ?: 0.0
    
    // HARDER XP Logic: 
    // 5 XP per set (was 15)
    // 1 XP per 20kg lifted (was 5 per 10kg equivalent)
    // 50 XP per workout session (was 100)
    val xpFromSets = totalSets * 5
    val xpFromVolume = (totalVolume / 20).toInt()
    val xpFromWorkouts = uniqueDates * 50
    val totalXp = xpFromSets + xpFromVolume + xpFromWorkouts
    
    // Level scaling: Each level requires more XP (Quadratic scaling)
    // Level 1: 0-2000 XP
    // Level 2: 2000-5000 XP
    // Level 3: 5000-9000 XP
    // Formula: nextLevelThreshold = level * 3000
    
    var tempXp = totalXp
    var level = 1
    var nextLevelThreshold = 2000
    
    while (tempXp >= nextLevelThreshold) {
        tempXp -= nextLevelThreshold
        level++
        nextLevelThreshold += 1000 // Levels get progressively harder
    }
    
    return UserStats(
        level = level,
        currentXp = tempXp,
        nextLevelXp = nextLevelThreshold,
        streak = calculateStreak(allSets.map { it.date }.distinct()),
        totalVolume = totalVolume,
        totalWorkouts = uniqueDates,
        totalReps = totalReps,
        totalSets = totalSets,
        maxWeight = maxWeight,
        max1RM = max1RM,
        exerciseCount = exercises.size,
        cycleCount = cycles.size
    )
}

private fun calculateStreak(dates: List<String>): Int {
    return dates.size 
}
