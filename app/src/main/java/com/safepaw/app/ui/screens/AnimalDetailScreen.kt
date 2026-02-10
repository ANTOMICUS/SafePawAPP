package com.safepaw.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.safepaw.app.data.models.Animal
import com.safepaw.app.ui.viewmodels.AnimalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalDetailScreen(
    animal: Animal,
    viewModel: AnimalViewModel,
    onBack: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    
    // Estados locales para edición
    var nombre by remember { mutableStateOf(animal.nombre) }
    var especie by remember { mutableStateOf(animal.especie) }
    var raza by remember { mutableStateOf(animal.raza) }
    var peso by remember { mutableStateOf(animal.peso.toString()) }
    var edad by remember { mutableStateOf(animal.edad.toString()) }
    var vacunasAlDia by remember { mutableStateOf(animal.vacunas_al_dia) }
    var estado by remember { mutableStateOf(animal.estado_adopcion) }
    var microchip by remember { mutableStateOf(animal.microchip) }
    var fotoUrl by remember { mutableStateOf(animal.foto_url) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Animal" else "Ficha Técnica") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            val updatedAnimal = animal.copy(
                                nombre = nombre,
                                especie = especie,
                                raza = raza,
                                peso = peso.toDoubleOrNull() ?: 0.0,
                                edad = edad.toIntOrNull() ?: 0,
                                vacunas_al_dia = vacunasAlDia,
                                estado_adopcion = estado,
                                microchip = microchip,
                                foto_url = fotoUrl
                            )
                            viewModel.upsertAnimal(updatedAnimal)
                            isEditing = false
                        }) {
                            Icon(Icons.Default.Save, contentDescription = "Guardar")
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto de Perfil
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (fotoUrl != null) {
                    AsyncImage(
                        model = fotoUrl,
                        contentDescription = "Foto de $nombre",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
                
                if (isEditing) {
                    IconButton(
                        onClick = { /* Implementar selector de imagen */ },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Cambiar foto")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DetailField(label = "Nombre", value = nombre, isEditing = isEditing, onValueChange = { nombre = it })
            DetailField(label = "Especie", value = especie, isEditing = isEditing, onValueChange = { especie = it })
            DetailField(label = "Raza", value = raza, isEditing = isEditing, onValueChange = { raza = it })
            DetailField(label = "Peso (kg)", value = peso, isEditing = isEditing, onValueChange = { peso = it })
            DetailField(label = "Edad", value = edad, isEditing = isEditing, onValueChange = { edad = it })
            
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Vacunas al día", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                if (isEditing) {
                    Switch(checked = vacunasAlDia, onCheckedChange = { vacunasAlDia = it })
                } else {
                    Text(if (vacunasAlDia) "Sí" else "No", style = MaterialTheme.typography.bodyLarge)
                }
            }

            DetailField(label = "Microchip", value = microchip, isEditing = isEditing, onValueChange = { microchip = it })
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Estado de Adopción", style = MaterialTheme.typography.labelLarge)
            if (isEditing) {
                // Simplificado: En una app real usaríamos un DropdownMenu
                OutlinedTextField(
                    value = estado,
                    onValueChange = { estado = it },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = estado,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Historial Médico", style = MaterialTheme.typography.headlineSmall)
            
            // Aquí se integrará el componente de Historial Médico en el siguiente paso
            Button(
                onClick = { /* Navegar a historial completo */ },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Ver Tratamientos")
            }
        }
    }
}

@Composable
fun DetailField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
