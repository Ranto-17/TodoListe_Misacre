package com.example.todolist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.Priorite
import com.example.todolist.data.Tache
import com.example.todolist.data.TacheRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TacheViewModel(private val repository: TacheRepository) : ViewModel() {

    // Tri automatique : HAUTE > MOYENNE > BASSE, puis par ID décroissant
    private fun List<Tache>.sortedByPriority(): List<Tache> {
        return this.sortedWith(
            compareByDescending<Tache> { it.priorite == Priorite.HAUTE }
                .thenByDescending { it.priorite == Priorite.MOYENNE }
                .thenByDescending { it.id }
        )
    }

    val allTaches: StateFlow<List<Tache>> = repository.allTaches
        .map { it.sortedByPriority() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tachesEnCours: StateFlow<List<Tache>> = repository.tachesEnCours
        .map { it.sortedByPriority() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tachesTerminees: StateFlow<List<Tache>> = repository.tachesTerminees
        .map { it.sortedByPriority() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pourcentage de progression
    val completionProgress: StateFlow<Float> = repository.allTaches
        .map { list ->
            if (list.isEmpty()) 0f
            else list.count { it.estTerminee }.toFloat() / list.size
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    fun insert(tache: Tache) = viewModelScope.launch { repository.insert(tache) }
    fun update(tache: Tache) = viewModelScope.launch { repository.update(tache) }
    fun delete(tache: Tache) = viewModelScope.launch { repository.delete(tache) }

    fun toggleTerminee(tache: Tache) = viewModelScope.launch {
        repository.update(tache.copy(estTerminee = !tache.estTerminee))
    }
}

class TacheViewModelFactory(private val repository: TacheRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TacheViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TacheViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
