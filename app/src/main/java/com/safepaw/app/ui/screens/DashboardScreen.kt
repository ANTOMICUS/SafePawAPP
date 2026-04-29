package com.safepaw.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.safepaw.app.data.models.Animal
import com.safepaw.app.ui.viewmodels.AnimalUiState
import com.safepaw.app.ui.viewmodels.AnimalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AnimalViewModel,
    onAnimalClick: (Animal) -> Unit,
    onScanClick: () -> Unit,
    onAddClick: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Filtros actuales
    var selectedEspecie by remember { mutableStateOf("Todos") }
    var selectedEstado by remember { mutableStateOf("Todos") }

    fun shareDocument(type: String) {
        val docTitle = if (type == "Adopción") "Contrato de Adopción" else "Contrato de Apadrinamiento"
        val docContent = if (type == "Adopción") {
            """
            📄 CONTRATO DE ADOPCIÓN - SAFE PAW 🐾
            ------------------------------------
            Por el presente documento, el adoptante se compromete a:
            1. Proporcionar cuidados veterinarios, alimentación y refugio.
            2. No abandonar, maltratar ni ceder al animal sin previo aviso.
            3. Mantener el microchip y vacunas al día.
            
            DATOS DEL ADOPTANTE:
            Nombre: __________________________
            DNI: _____________________________
            Dirección: ________________________
            Teléfono: _________________________
            
            Firma: ___________________________
            ------------------------------------
            Enviado desde SafePaw App
            """.trimIndent()
        } else {
            """
            📄 CONTRATO DE APADRINAMIENTO - SAFE PAW 🐾
            ------------------------------------
            Por el presente documento, el padrino/madrina se compromete a:
            1. Contribuir mensualmente al bienestar del animal apadrinado.
            2. Recibir actualizaciones periódicas sobre su estado.
            3. Visitar al animal bajo coordinación del centro.
            
            DATOS DEL PADRINO/MADRINA:
            Nombre: __________________________
            DNI: _____________________________
            Email: ___________________________
            Cuota mensual: ___________________
            
            Firma: ___________________________
            ------------------------------------
            Enviado desde SafePaw App
            """.trimIndent()
        }

        val intentToSend = Intent(Intent.ACTION_SEND)
        intentToSend.type = "text/plain"
        intentToSend.putExtra(Intent.EXTRA_SUBJECT, docTitle)
        intentToSend.putExtra(Intent.EXTRA_TEXT, docContent)
        
        context.startActivity(Intent.createChooser(intentToSend, "Enviar $docTitle"))
    }

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
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text("Documento Adopción") },
                            onClick = {
                                showMenu = false
                                shareDocument("Adopción")
                            },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Documento Apadrinar") },
                            onClick = {
                                showMenu = false
                                shareDocument("Apadrinar")
                            },
                            leadingIcon = { Icon(Icons.Default.VolunteerActivism, contentDescription = null) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                showMenu = false
                                onLogout()
                            },
                            leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null) }
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
    val estados = listOf("Todos", "Apadrinar", "En Adopción", "Adoptado", "Urgente")

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
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen de perfil en la lista
                    if (animal.foto_url != null) {
                        AsyncImage(
                            model = animal.foto_url,
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                Icons.Default.Pets,
                                contentDescription = null,
                                modifier = Modifier.padding(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(text = animal.nombre, style = MaterialTheme.typography.headlineSmall)
                        Text(text = "Especie: ${animal.especie}")
                        Text(text = "Microchip: ${animal.microchip}")
                        Text(text = "Estado: ${animal.estado_adopcion}")
                    }
                }
            }
        }
    }
}
