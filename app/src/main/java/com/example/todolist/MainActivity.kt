package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.data.AppDatabase
import com.example.todolist.data.Priorite
import com.example.todolist.data.Tache
import com.example.todolist.data.TacheRepository
import com.example.todolist.ui.TacheViewModel
import com.example.todolist.ui.TacheViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getInstance(this)
        val repository = TacheRepository(database.tacheDao())
        val factory = TacheViewModelFactory(repository)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: TacheViewModel = viewModel(factory = factory)
                    TodoListScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(viewModel: TacheViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    val allTaches by viewModel.allTaches.collectAsState()
    val enCours by viewModel.tachesEnCours.collectAsState()
    val terminees by viewModel.tachesTerminees.collectAsState()
    val progress by viewModel.completionProgress.collectAsState()

    val currentList = when (selectedTab) {
        0 -> allTaches
        1 -> enCours
        else -> terminees
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Ma TodoList MVVM") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Progression
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Progression", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% des tâches terminées",
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Onglets de filtrage
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Toutes") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("En cours") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Terminées") })
            }

            // Liste
            if (currentList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucune tâche ici !", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList, key = { it.id }) { tache ->
                        TacheItem(
                            tache = tache,
                            onToggle = { viewModel.toggleTerminee(tache) },
                            onDelete = { viewModel.delete(tache) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddTacheDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { titre, desc, prio ->
                    viewModel.insert(Tache(titre = titre, description = desc, priorite = prio))
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TacheItem(tache: Tache, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tache.estTerminee) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = tache.estTerminee, onCheckedChange = { onToggle() })
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = tache.titre,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (tache.estTerminee) Color.Gray else Color.Unspecified
                    )
                )
                if (!tache.description.isNullOrBlank()) {
                    Text(text = tache.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
                
                val color = when(tache.priorite) {
                    Priorite.HAUTE -> Color(0xFFD32F2F)
                    Priorite.MOYENNE -> Color(0xFFFBC02D)
                    Priorite.BASSE -> Color(0xFF388E3C)
                }

                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = tache.priorite.name,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        color = color,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun AddTacheDialog(onDismiss: () -> Unit, onConfirm: (String, String, Priorite) -> Unit) {
    var titre by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var prio by remember { mutableStateOf(Priorite.MOYENNE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle tâche") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titre, 
                    onValueChange = { titre = it }, 
                    label = { Text("Titre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc, 
                    onValueChange = { desc = it }, 
                    label = { Text("Description (optionnelle)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Niveau de priorité :", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Priorite.entries.forEach { p ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            RadioButton(selected = prio == p, onClick = { prio = p })
                            Text(p.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (titre.isNotBlank()) onConfirm(titre, desc, prio) },
                enabled = titre.isNotBlank()
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
