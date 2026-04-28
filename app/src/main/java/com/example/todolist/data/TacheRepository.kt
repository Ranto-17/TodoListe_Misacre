package com.example.todolist.data

import kotlinx.coroutines.flow.Flow

class TacheRepository(private val tacheDao: TacheDao) {

    val allTaches: Flow<List<Tache>> = tacheDao.getAllTaches()
    val tachesEnCours: Flow<List<Tache>> = tacheDao.getTachesEnCours()
    val tachesTerminees: Flow<List<Tache>> = tacheDao.getTachesTerminees()

    suspend fun insert(tache: Tache) {
        tacheDao.insert(tache)
    }

    suspend fun update(tache: Tache) {
        tacheDao.update(tache)
    }

    suspend fun delete(tache: Tache) {
        tacheDao.delete(tache)
    }
}
