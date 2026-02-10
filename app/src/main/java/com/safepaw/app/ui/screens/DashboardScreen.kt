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
    var showMenu by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Filtros actuales
    var selectedEspecie by remember { mutableStateOf("Todos") }
    var selectedEstado by remember { mutableStateOf("Todos") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SafePaw - Dashboard") },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Añadir Animal") },
                            onClick = {
                                showMenu = false
                                onAddClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Filtrar Búsqueda") },
                            onClick = {
                                showMenu = false
                                showFilterDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onScanClick) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.filterAnimales(it, selectedEspecie, selectedEstado)
                },
                label = { Text("Buscar por nombre, chip o raza...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            searchQuery = ""
                            viewModel.filterAnimales("", selectedEspecie, selectedEstado)
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                }
            )

            if (selectedEspecie != "Todos" || selectedEstado != "Todos") {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (selectedEspecie != "Todos") {
                        FilterChip(
                            selected = true,
                            onClick = { 
                                selectedEspecie = "Todos"
                                viewModel.filterAnimales(searchQuery, "Todos", selectedEstado)
                            },
                            label = { Text(selectedEspecie) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (selectedEstado != "Todos") {
                        FilterChip(
                            selected = true,
                            onClick = { 
                                selectedEstado = "Todos"
                                viewModel.filterAnimales(searchQuery, selectedEspecie, "Todos")
                            },
                            label = { Text(selectedEstado) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            when (val state = uiState) {
                is AnimalUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AnimalUiState.Success -> {
                    AnimalList(state.animales, onAnimalClick)
                }
                is AnimalUiState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
            }
        }

        if (showFilterDialog) {
            FilterDialog(
                currentEspecie = selectedEspecie,
                currentEstado = selectedEstado,
                onDismiss = { showFilterDialog = false },
                onApply = { especie, estado ->
                    selectedEspecie = especie
                    selectedEstado = estado
                    viewModel.filterAnimales(searchQuery, especie, estado)
                    showFilterDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentEspecie: String,
    currentEstado: String,
    onDismiss: () -> Unit,
    onApply: (String, String) -> Unit
) {
    var especie by remember { mutableStateOf(currentEspecie) }
    var estado by remember { mutableStateOf(currentEstado) }

    val especies = listOf("Todos", "Perro", "Gato", "Otro")
    val estados = listOf("Todos", "Disponible", "En Adopción", "Adoptado", "Urgente")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar Animales") },
        text = {
            Column {
                Text("Especie", style = MaterialTheme.typography.labelLarge)
                especies.forEach { esp ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (especie == esp), onClick = { especie = esp })
                        Text(esp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Estado", style = MaterialTheme.typography.labelLarge)
                estados.forEach { est ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (estado == est), onClick = { estado = est })
                        Text(est)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApply(especie, estado) }) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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
