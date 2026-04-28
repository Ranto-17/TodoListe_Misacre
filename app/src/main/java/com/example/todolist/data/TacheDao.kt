package com.example.todolist.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TacheDao {
    @Query("SELECT * FROM taches ORDER BY id DESC")
    fun getAllTaches(): Flow<List<Tache>>

    @Query("SELECT * FROM taches WHERE estTerminee = 0 ORDER BY id DESC")
    fun getTachesEnCours(): Flow<List<Tache>>

    @Query("SELECT * FROM taches WHERE estTerminee = 1 ORDER BY id DESC")
    fun getTachesTerminees(): Flow<List<Tache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tache: Tache)

    @Update
    suspend fun update(tache: Tache)

    @Delete
    suspend fun delete(tache: Tache)
}
