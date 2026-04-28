package com.example.todolist.data

import android.content.Context
import androidx.room.*

class Converters {
    @TypeConverter
    fun fromPriorite(priorite: Priorite): String {
        return priorite.name
    }

    @TypeConverter
    fun toPriorite(value: String): Priorite {
        return Priorite.valueOf(value)
    }
}

@Database(entities = [Tache::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tacheDao(): TacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tache_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
