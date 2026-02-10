package com.safepaw.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.safepaw.app.data.models.Animal
import com.safepaw.app.ui.viewmodels.AnimalUiState
import com.safepaw.app.ui.viewmodels.AnimalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AnimalViewModel,
    onAnimalClick: (Animal) -> Unit,
    onScanClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("SafePaw - Dashboard") })
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = onAddClick,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Manual")
                }
                FloatingActionButton(onClick = onScanClick) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    if (it.length >= 5) viewModel.searchByMicrochip(it)
                    else if (it.isEmpty()) viewModel.fetchAnimales()
                },
                label = { Text("Buscar microchip...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            when (val state = uiState) {
                is AnimalUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                is AnimalUiState.Success -> {
                    AnimalList(state.animales, onAnimalClick)
                }
                is AnimalUiState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun AnimalList(animales: List<Animal>, onAnimalClick: (Animal) -> Unit) {
    LazyColumn {
        items(animales) { animal ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                onClick = { onAnimalClick(animal) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = animal.nombre, style = MaterialTheme.typography.headlineSmall)
                    Text(text = "Especie: ${animal.especie}")
                    Text(text = "Microchip: ${animal.microchip}")
                    Text(text = "Estado: ${animal.estado_adopcion}")
                }
            }
        }
    }
}
