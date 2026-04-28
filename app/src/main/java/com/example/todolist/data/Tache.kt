package com.example.todolist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priorite {
    BASSE, MOYENNE, HAUTE
}

@Entity(tableName = "taches")
data class Tache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titre: String,
    val description: String?,
    val priorite: Priorite,
    val estTerminee: Boolean = false
)
