package com.safepaw.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.safepaw.app.data.models.Animal
import com.safepaw.app.ui.viewmodels.AnimalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalAddScreen(
    viewModel: AnimalViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var vacunasAlDia by remember { mutableStateOf(false) }
    var microchip by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("Disponible") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Registro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (nombre.isNotBlank() && microchip.isNotBlank()) {
                            val newAnimal = Animal(
                                nombre = nombre,
                                especie = especie,
                                raza = raza,
                                peso = peso.toDoubleOrNull() ?: 0.0,
                                edad = edad.toIntOrNull() ?: 0,
                                vacunas_al_dia = vacunasAlDia,
                                microchip = microchip,
                                estado_adopcion = estado
                            )
                            viewModel.upsertAnimal(newAnimal)
                            onSuccess()
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
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
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = especie,
                onValueChange = { especie = it },
                label = { Text("Especie") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = raza,
                onValueChange = { raza = it },
                label = { Text("Raza") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it },
                label = { Text("Peso (kg)") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = edad,
                onValueChange = { edad = it },
                label = { Text("Edad") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = microchip,
                onValueChange = { microchip = it },
                label = { Text("Número de Chip") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Vacunas al día", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Switch(checked = vacunasAlDia, onCheckedChange = { vacunasAlDia = it })
            }

            OutlinedTextField(
                value = estado,
                onValueChange = { estado = it },
                label = { Text("Estado de Adopción") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (nombre.isNotBlank() && microchip.isNotBlank()) {
                        val newAnimal = Animal(
                            nombre = nombre,
                            especie = especie,
                            raza = raza,
                            peso = peso.toDoubleOrNull() ?: 0.0,
                            edad = edad.toIntOrNull() ?: 0,
                            vacunas_al_dia = vacunasAlDia,
                            microchip = microchip,
                            estado_adopcion = estado
                        )
                        viewModel.upsertAnimal(newAnimal)
                        onSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar Animal")
            }
        }
    }
}
