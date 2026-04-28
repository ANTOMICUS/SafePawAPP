package com.safepaw.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.safepaw.app.data.models.Animal
import com.safepaw.app.ui.navigation.Screen
import com.safepaw.app.ui.viewmodels.AnimalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalDetailScreen(
    animal: Animal,
    viewModel: AnimalViewModel,
    navController: androidx.navigation.NavController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }

    // Asegura que el gesto/botón físico de "atrás" ejecuta el mismo flujo que el botón de la app.
    BackHandler { onBack() }
    
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

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            if (bytes != null) {
                viewModel.uploadPhoto(animal.id_animal, bytes) { newUrl ->
                    fotoUrl = newUrl
                }
            }
        }
    }

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
            // Cabecera: foto + nombre
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape),
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
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Pets,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isEditing) {
                    FilledIconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Cambiar foto")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = nombre.ifBlank { "Sin nombre" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = microchip.ifBlank { "Sin microchip" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))

                    LabeledField(
                        label = "Nombre",
                        value = nombre,
                        isEditing = isEditing,
                        onValueChange = { nombre = it }
                    )
                    LabeledField(
                        label = "Especie",
                        value = especie,
                        isEditing = isEditing,
                        onValueChange = { especie = it }
                    )
                    LabeledField(
                        label = "Raza",
                        value = raza,
                        isEditing = isEditing,
                        onValueChange = { raza = it }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            LabeledField(
                                label = "Peso (kg)",
                                value = peso,
                                isEditing = isEditing,
                                onValueChange = { peso = it }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            LabeledField(
                                label = "Edad",
                                value = edad,
                                isEditing = isEditing,
                                onValueChange = { edad = it }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vacunas al día", style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = if (vacunasAlDia) "Sí" else "No",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isEditing) {
                            Switch(checked = vacunasAlDia, onCheckedChange = { vacunasAlDia = it })
                        } else {
                            AssistChip(
                                onClick = {},
                                label = { Text(if (vacunasAlDia) "Sí" else "No") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LabeledField(
                        label = "Microchip",
                        value = microchip,
                        isEditing = isEditing,
                        onValueChange = { microchip = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estado de adopción", style = MaterialTheme.typography.labelLarge)
                    if (isEditing) {
                        OutlinedTextField(
                            value = estado,
                            onValueChange = { estado = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        )
                    } else {
                        AssistChip(
                            onClick = {},
                            label = { Text(estado) },
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Historial Médico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            navController.navigate(Screen.MedicalHistory.createRoute(animal.id_animal, animal.nombre))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver / Editar historial")
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                singleLine = true
            )
        } else {
            Text(
                text = value.ifBlank { "-" },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
