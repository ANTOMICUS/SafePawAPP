package com.safepaw.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
                onConfirm = { tipo, descripcion, duracion ->
                    val nuevoTratamiento = Tratamiento(
                        id_animal = idAnimal,
                        tipo = tipo,
                        descripcion = descripcion,
                        fecha = java.time.LocalDateTime.now().toString(),
                        duracion = duracion
                    )
                    viewModel.addTratamiento(nuevoTratamiento)
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (tratamiento.tipo == "Intervención") Icons.Default.MedicalInformation else Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = tratamiento.tipo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Fecha: ${tratamiento.fecha.take(10)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (tratamiento.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = tratamiento.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
            if (tratamiento.duracion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Duración: ${tratamiento.duracion}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun AddTreatmentDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var tipo by remember { mutableStateOf("Tratamiento") }
    var descripcion by remember { mutableStateOf("") }
    var duracion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Registro Médico") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = tipo == "Tratamiento", onClick = { tipo = "Tratamiento" })
                    Text("Tratamiento")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = tipo == "Intervención", onClick = { tipo = "Intervención" })
                    Text("Intervención")
                }
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = duracion,
                    onValueChange = { duracion = it },
                    label = { Text("Duración (ej: 7 días, 2h)") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(tipo, descripcion, duracion) }, enabled = descripcion.isNotBlank()) {
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
