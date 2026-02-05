package com.example.myapplication.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :exerciseId")
    fun getSetsForExercise(exerciseId: Long): Flow<List<ExerciseSet>>

    @Insert
    suspend fun insertSet(set: ExerciseSet)

    @Delete
    suspend fun deleteSet(set: ExerciseSet)

    // Cycle support
    @Query("SELECT * FROM training_cycles ORDER BY id DESC")
    fun getAllCycles(): Flow<List<TrainingCycle>>

    @Insert
    suspend fun insertCycle(cycle: TrainingCycle)

    @Update
    suspend fun updateCycle(cycle: TrainingCycle)

    @Query("SELECT * FROM training_cycles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveCycle(): TrainingCycle?

    // Battle Scars
    @Query("SELECT * FROM battle_scars ORDER BY id DESC")
    fun getAllBattleScars(): Flow<List<BattleScar>>

    @Insert
    suspend fun insertBattleScar(scar: BattleScar)

    @Delete
    suspend fun deleteBattleScar(scar: BattleScar)
}

@Database(entities = [Exercise::class, ExerciseSet::class, TrainingCycle::class, BattleScar::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
