package com.safepaw.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.safepaw.app.data.models.Tratamiento
import com.safepaw.app.ui.viewmodels.AnimalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreen(
    idAnimal: String,
    nombreAnimal: String,
    userRole: String, // "Vet", "Gestor", "Voluntario"
    viewModel: AnimalViewModel,
    onBack: () -> Unit
) {
    val tratamientos by viewModel.selectedAnimalTratamientos.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(idAnimal) {
        viewModel.fetchTratamientos(idAnimal)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial: $nombreAnimal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            // Solo Vet y Gestor pueden añadir tratamientos
            if (userRole == "Vet" || userRole == "Gestor") {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Tratamiento")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (tratamientos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay tratamientos registrados")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(tratamientos) { tratamiento ->
                        TreatmentItem(tratamiento)
                    }
                }
            }
        }

        if (showAddDialog) {
            AddTreatmentDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { tipo ->
                    // Aquí se llamaría a viewModel.addTratamiento(idAnimal, tipo)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TreatmentItem(tratamiento: Tratamiento) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = tratamiento.tipo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = "Fecha: ${tratamiento.fecha}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun AddTreatmentDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var tipo by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Tratamiento") },
        text = {
            OutlinedTextField(
                value = tipo,
                onValueChange = { tipo = it },
                label = { Text("Descripción del tratamiento") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(tipo) }, enabled = tipo.isNotBlank()) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
