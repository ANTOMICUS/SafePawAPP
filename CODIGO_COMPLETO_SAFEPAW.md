# Código Completo de SafePaw App 🐾

Este documento contiene todo el código fuente de la aplicación SafePaw, organizado por archivos.

---

## 1. Configuración del Proyecto

### `app/build.gradle.kts`
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android") version "2.50"
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
}

android {
    namespace = "com.example.safepaw"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.safepaw"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.6.1")
    implementation("androidx.compose.ui:ui-graphics:1.6.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.1")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("javax.inject:javax.inject:1")

    // SUPABASE
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.5.0")
    
    // KTOR (Versión fija para estabilidad)
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // CameraX & ML Kit
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Coil para carga de imágenes
    implementation("io.coil-kt:coil-compose:2.6.0")
}
```

---

## 2. Modelos y Datos

### `app/src/main/java/com/safepaw/app/data/models/Models.kt`
```kotlin
package com.safepaw.app.data.models

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Animal(
    val id_animal: String = UUID.randomUUID().toString(),
    val microchip: String,
    val nombre: String,
    val especie: String,
    val raza: String = "",
    val peso: Double = 0.0,
    val edad: Int = 0,
    val vacunas_al_dia: Boolean = false,
    val estado_adopcion: String,
    val foto_url: String? = null
)

@Serializable
data class Tratamiento(
    val id_tratamiento: String = UUID.randomUUID().toString(),
    val id_animal: String,
    val tipo: String, // Tratamiento o Intervención
    val descripcion: String = "",
    val fecha: String, // ISO 8601
    val duracion: String = "" // Ej: "7 días", "2 horas"
)

@Serializable
data class Usuario(
    val id_usuario: String,
    val nombre: String,
    val rol: String // Gestor, Voluntario, Vet
)

@Serializable
data class UsuarioRegistro(
    val id_usuario: String,
    val nombre: String,
    val rol: String,
    val mail: String,
    val contrasena: String,
    val codigo_usuario: String
)

@Serializable
data class Adopcion(
    val id_adopcion: String = UUID.randomUUID().toString(),
    val id_animal: String,
    val id_usuario: String,
    val estado: String
)

@Serializable
data class AnimalFoto(
    val id_foto: String = UUID.randomUUID().toString(),
    val id_animal: String,
    val url: String,
    val created_at: String? = null
)
```

### `app/src/main/java/com/safepaw/app/data/repository/SupabaseRepository.kt`
```kotlin
package com.safepaw.app.data.repository

import com.safepaw.app.data.models.Animal
import com.safepaw.app.data.models.AnimalFoto
import com.safepaw.app.data.models.Tratamiento
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    // --- Animales ---
    suspend fun getAllAnimales(): List<Animal> = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["animales"].select().decodeList<Animal>()
    }

    suspend fun getAnimalByMicrochip(microchip: String): Animal? = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["animales"]
            .select {
                filter {
                    eq("microchip", microchip)
                }
            }.decodeSingleOrNull<Animal>()
    }

    suspend fun insertAnimal(animal: Animal) = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["animales"].insert(animal)
    }

    suspend fun updateAnimal(animal: Animal) = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["animales"].update(animal) {
            filter {
                eq("id_animal", animal.id_animal)
            }
        }
    }

    suspend fun deleteAnimal(id: String) = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["animales"].delete {
            filter {
                eq("id_animal", id)
            }
        }
    }

    // --- Tratamientos ---
    suspend fun getTratamientosByAnimal(idAnimal: String): List<Tratamiento> = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["tratamientos"]
            .select {
                filter {
                    eq("id_animal", idAnimal)
                }
                order("fecha", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }.decodeList<Tratamiento>()
    }

    suspend fun insertTratamiento(tratamiento: Tratamiento) = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["tratamientos"].insert(tratamiento)
    }

    // --- Imágenes ---
    suspend fun uploadAnimalPhoto(idAnimal: String, bytes: ByteArray): String = withContext(Dispatchers.IO) {
        val fileName = "$idAnimal.jpg"
        val bucket = supabaseClient.storage["animal-photos"]
        bucket.upload(fileName, bytes, upsert = true)
        bucket.publicUrl(fileName)
    }

    suspend fun getAnimalFotos(idAnimal: String): List<AnimalFoto> = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["animal_fotos"]
            .select {
                filter { eq("id_animal", idAnimal) }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<AnimalFoto>()
    }

    suspend fun uploadAnimalGalleryPhoto(idAnimal: String, bytes: ByteArray): String = withContext(Dispatchers.IO) {
        val fileName = "$idAnimal/${UUID.randomUUID()}.jpg"
        val bucket = supabaseClient.storage["animal-photos"]
        bucket.upload(fileName, bytes, upsert = false)
        bucket.publicUrl(fileName)
    }

    suspend fun insertAnimalFoto(foto: AnimalFoto) = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["animal_fotos"].insert(foto)
    }

    suspend fun searchAnimales(
        query: String? = null,
        especie: String? = null,
        estado: String? = null
    ): List<Animal> = withContext(Dispatchers.IO) {
        supabaseClient.postgrest["animales"].select {
            filter {
                if (!query.isNullOrBlank()) {
                    or {
                        ilike("nombre", "%$query%")
                        ilike("microchip", "%$query%")
                        ilike("raza", "%$query%")
                    }
                }
                if (!especie.isNullOrBlank() && especie != "Todos") eq("especie", especie)
                if (!estado.isNullOrBlank() && estado != "Todos") eq("estado_adopcion", estado)
            }
        }.decodeList<Animal>()
    }
}
```

---

## 3. Interfaz de Usuario (UI)

### `app/src/main/java/com/safepaw/app/ui/screens/DashboardScreen.kt`
```kotlin
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
        
        context.startActivity(Intent.createChooser(intentToSend, "Enviar ${docTitle}"))
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
```

### `app/src/main/java/com/safepaw/app/ui/screens/AnimalDetailScreen.kt`
```kotlin
package com.safepaw.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
    val estados = remember { listOf("Apadrinar", "En Adopción", "Adoptado", "Urgente") }
    var estadoExpanded by remember { mutableStateOf(false) }

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

    val galleryMultiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val bytesList = uris.mapNotNull { uri ->
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }
        if (bytesList.isNotEmpty()) {
            viewModel.addAnimalFotos(animal.id_animal, bytesList)
        }
    }

    val fotosMap by viewModel.animalFotos.collectAsState()
    val fotos = fotosMap[animal.id_animal].orEmpty()
    val tratamientos by viewModel.selectedAnimalTratamientos.collectAsState()

    LaunchedEffect(animal.id_animal) {
        viewModel.fetchAnimalFotos(animal.id_animal)
        viewModel.fetchTratamientos(animal.id_animal)
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
                    IconButton(
                        onClick = {
                            val texto = buildString {
                                appendLine("🐾 Perfil de ${nombre.ifBlank { animal.nombre }} 🐾")
                                appendLine("----------------------------------")
                                appendLine("🔹 Especie: ${especie.ifBlank { animal.especie }}")
                                appendLine("🔹 Raza: ${raza.ifBlank { animal.raza }}")
                                appendLine("🔹 Peso: ${peso.ifBlank { animal.peso }} kg")
                                appendLine("🔹 Edad: ${edad.ifBlank { animal.edad }}")
                                appendLine("🔹 Estado: ${estado.ifBlank { animal.estado_adopcion }}")
                                appendLine("🔹 Microchip: ${microchip.ifBlank { animal.microchip }}")
                                appendLine("🔹 Vacunas al día: ${if (vacunasAlDia) "Sí ✅" else "No ❌"}")
                                
                                if (!fotoUrl.isNullOrBlank()) {
                                    appendLine()
                                    appendLine("📸 Foto de perfil: $fotoUrl")
                                }

                                if (fotos.isNotEmpty()) {
                                    appendLine()
                                    appendLine("🖼️ Galería de fotos:")
                                    fotos.forEachIndexed { index, foto ->
                                        appendLine("${index + 1}. ${foto.url}")
                                    }
                                }

                                if (tratamientos.isNotEmpty()) {
                                    appendLine()
                                    appendLine("🏥 Historial Médico:")
                                    tratamientos.take(5).forEach { t ->
                                        appendLine("- ${t.fecha.take(10)}: ${t.tipo} (${t.descripcion})")
                                    }
                                    if (tratamientos.size > 5) appendLine("- ... (más en la app)")
                                }
                                appendLine("----------------------------------")
                                appendLine("Compartido desde SafePaw App")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "SafePaw - Perfil de ${nombre.ifBlank { animal.nombre }}")
                                putExtra(Intent.EXTRA_TEXT, texto)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir perfil completo"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
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
                        ExposedDropdownMenuBox(
                            expanded = estadoExpanded,
                            onExpandedChange = { estadoExpanded = !estadoExpanded },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        ) {
                            OutlinedTextField(
                                value = estado,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = estadoExpanded,
                                onDismissRequest = { estadoExpanded = false }
                            ) {
                                estados.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion) },
                                        onClick = {
                                            estado = opcion
                                            estadoExpanded = false
                                        }
                                    )
                                }
                            }
                        }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fotos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.weight(1f))
                        if (isEditing) {
                            FilledTonalIconButton(onClick = { galleryMultiLauncher.launch("image/*") }) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Añadir fotos")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (fotos.isEmpty()) {
                        Text(
                            text = if (isEditing) "Añade fotos adicionales del animal." else "No hay fotos adicionales.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(fotos, key = { it.id_foto }) { foto ->
                                Card(
                                    modifier = Modifier.size(120.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    AsyncImage(
                                        model = foto.url,
                                        contentDescription = "Foto",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
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
```

### `app/src/main/java/com/safepaw/app/ui/screens/AnimalAddScreen.kt`
```kotlin
package com.safepaw.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
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
import com.safepaw.app.ui.viewmodels.AnimalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalAddScreen(
    viewModel: AnimalViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var vacunasAlDia by remember { mutableStateOf(false) }
    var microchip by remember { mutableStateOf("") }
    val estados = remember { listOf("Apadrinar", "En Adopción", "Adoptado", "Urgente") }
    var estado by remember { mutableStateOf(estados.first()) }
    var estadoExpanded by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    fun saveAnimal() {
        if (nombre.isNotBlank() && microchip.isNotBlank()) {
            val animalId = java.util.UUID.randomUUID().toString()
            
            if (imageUri != null) {
                val inputStream = context.contentResolver.openInputStream(imageUri!!)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    viewModel.uploadPhoto(animalId, bytes) { url ->
                        val newAnimal = Animal(
                            id_animal = animalId,
                            nombre = nombre,
                            especie = especie,
                            raza = raza,
                            peso = peso.toDoubleOrNull() ?: 0.0,
                            edad = edad.toIntOrNull() ?: 0,
                            vacunas_al_dia = vacunasAlDia,
                            microchip = microchip,
                            estado_adopcion = estado,
                            foto_url = url
                        )
                        viewModel.upsertAnimal(newAnimal)
                        onSuccess()
                    }
                }
            } else {
                val newAnimal = Animal(
                    id_animal = animalId,
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
        }
    }

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
                    IconButton(onClick = { saveAnimal() }) {
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Selector de Imagen
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = null,
                            modifier = Modifier.padding(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Seleccionar foto",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            ExposedDropdownMenuBox(
                expanded = estadoExpanded,
                onExpandedChange = { estadoExpanded = !estadoExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = estado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado de Adopción") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = estadoExpanded,
                    onDismissRequest = { estadoExpanded = false }
                ) {
                    estados.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                estado = opcion
                                estadoExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { saveAnimal() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar Animal")
            }
        }
    }
}
```

---

## 4. Lógica de Negocio y Navegación

### `app/src/main/java/com/safepaw/app/ui/viewmodels/AnimalViewModel.kt`
```kotlin
package com.safepaw.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safepaw.app.data.models.Animal
import com.safepaw.app.data.models.AnimalFoto
import com.safepaw.app.data.models.Tratamiento
import com.safepaw.app.data.repository.SupabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AnimalUiState {
    object Loading : AnimalUiState()
    data class Success(val animales: List<Animal>) : AnimalUiState()
    data class Error(val message: String) : AnimalUiState()
}

@HiltViewModel
class AnimalViewModel @Inject constructor(
    private val repository: SupabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnimalUiState>(AnimalUiState.Loading)
    val uiState: StateFlow<AnimalUiState> = _uiState.asStateFlow()

    private val _selectedAnimalTratamientos = MutableStateFlow<List<Tratamiento>>(emptyList())
    val selectedAnimalTratamientos: StateFlow<List<Tratamiento>> = _selectedAnimalTratamientos.asStateFlow()

    private val _animalFotos = MutableStateFlow<Map<String, List<AnimalFoto>>>(emptyMap())
    val animalFotos: StateFlow<Map<String, List<AnimalFoto>>> = _animalFotos.asStateFlow()

    init {
        fetchAnimales()
    }

    fun fetchAnimales() {
        viewModelScope.launch {
            _uiState.value = AnimalUiState.Loading
            try {
                val lista = repository.getAllAnimales()
                _uiState.value = AnimalUiState.Success(lista)
            } catch (e: Exception) {
                _uiState.value = AnimalUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun searchByMicrochip(microchip: String) {
        viewModelScope.launch {
            try {
                val animal = repository.getAnimalByMicrochip(microchip)
                if (animal != null) {
                    val current = _uiState.value
                    if (current is AnimalUiState.Success) {
                        val updated = current.animales.toMutableList()
                        val idx = updated.indexOfFirst { it.id_animal == animal.id_animal || it.microchip == microchip }
                        if (idx >= 0) updated[idx] = animal else updated.add(animal)
                        _uiState.value = AnimalUiState.Success(updated)
                    } else {
                        _uiState.value = AnimalUiState.Success(listOf(animal))
                    }
                } else {
                    val current = _uiState.value
                    if (current !is AnimalUiState.Success) {
                        _uiState.value = AnimalUiState.Error("No se encontró ningún animal con ese microchip")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AnimalUiState.Error(e.message ?: "Error en la búsqueda")
            }
        }
    }

    fun getAnimalByMicrochipFromList(microchip: String): Animal? {
        val state = _uiState.value
        return if (state is AnimalUiState.Success) {
            state.animales.find { it.microchip == microchip }
        } else null
    }

    fun fetchTratamientos(idAnimal: String) {
        viewModelScope.launch {
            try {
                val tratamientos = repository.getTratamientosByAnimal(idAnimal)
                _selectedAnimalTratamientos.value = tratamientos
            } catch (e: Exception) {
            }
        }
    }

    fun upsertAnimal(animal: Animal) {
        viewModelScope.launch {
            try {
                val current = _uiState.value
                val exists = (current as? AnimalUiState.Success)
                    ?.animales
                    ?.any { it.id_animal == animal.id_animal } == true

                if (exists) {
                    repository.updateAnimal(animal)
                } else {
                    repository.insertAnimal(animal)
                }
                fetchAnimales()
            } catch (e: Exception) {
                _uiState.value = AnimalUiState.Error("Error al guardar animal")
            }
        }
    }

    fun addTratamiento(tratamiento: Tratamiento) {
        viewModelScope.launch {
            try {
                repository.insertTratamiento(tratamiento)
                fetchTratamientos(tratamiento.id_animal)
            } catch (e: Exception) {
            }
        }
    }

    fun uploadPhoto(idAnimal: String, bytes: ByteArray, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val url = repository.uploadAnimalPhoto(idAnimal, bytes)
                onComplete(url)
            } catch (e: Exception) {
            }
        }
    }

    fun fetchAnimalFotos(idAnimal: String) {
        viewModelScope.launch {
            try {
                val fotos = repository.getAnimalFotos(idAnimal)
                _animalFotos.value = _animalFotos.value.toMutableMap().apply { put(idAnimal, fotos) }
            } catch (_: Exception) {
            }
        }
    }

    fun addAnimalFotos(idAnimal: String, bytesList: List<ByteArray>) {
        viewModelScope.launch {
            try {
                bytesList.forEach { bytes ->
                    val url = repository.uploadAnimalGalleryPhoto(idAnimal, bytes)
                    repository.insertAnimalFoto(
                        AnimalFoto(
                            id_animal = idAnimal,
                            url = url
                        )
                    )
                }
                fetchAnimalFotos(idAnimal)
            } catch (_: Exception) {
            }
        }
    }

    fun filterAnimales(query: String?, especie: String?, estado: String?) {
        viewModelScope.launch {
            _uiState.value = AnimalUiState.Loading
            try {
                val lista = repository.searchAnimales(query, especie, estado)
                _uiState.value = AnimalUiState.Success(lista)
            } catch (e: Exception) {
                _uiState.value = AnimalUiState.Error(e.message ?: "Error en el filtrado")
            }
        }
    }
}
```

### `app/src/main/java/com/safepaw/app/ui/navigation/SafePawNavigation.kt`
```kotlin
package com.safepaw.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.safepaw.app.ui.screens.*
import com.safepaw.app.ui.viewmodels.AnimalUiState
import com.safepaw.app.ui.viewmodels.AnimalViewModel
import com.safepaw.app.ui.viewmodels.AuthViewModel
import com.safepaw.app.ui.viewmodels.AuthState

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Scanner : Screen("scanner")
    object AnimalAdd : Screen("animal_add")
    object AnimalDetail : Screen("animal_detail/{microchip}") {
        fun createRoute(microchip: String) = "animal_detail/$microchip"
    }
    object MedicalHistory : Screen("medical_history/{id}/{nombre}") {
        fun createRoute(id: String, nombre: String) = "medical_history/$id/$nombre"
    }
}

@Composable
fun SafePawNavigation(
    authViewModel: AuthViewModel,
    animalViewModel: AnimalViewModel
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    if (authState is AuthState.Loading) {
        Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (authState is AuthState.Authenticated) {
        Screen.Dashboard.route
    } else {
        Screen.Login.route
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                if (navController.currentDestination?.route == Screen.Login.route) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            AuthState.Idle -> {
                if (navController.currentDestination?.route != Screen.Login.route) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            }
            else -> Unit
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = animalViewModel,
                onAnimalClick = { animal ->
                    navController.navigate(Screen.AnimalDetail.createRoute(animal.microchip))
                },
                onScanClick = {
                    navController.navigate(Screen.Scanner.route)
                },
                onAddClick = {
                    navController.navigate(Screen.AnimalAdd.route)
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AnimalAdd.route) {
            AnimalAddScreen(
                viewModel = animalViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Scanner.route) {
            ScannerScreen(
                onBarcodeDetected = { microchip ->
                    navController.navigate(Screen.AnimalDetail.createRoute(microchip)) {
                        popUpTo(Screen.Scanner.route) { inclusive = true }
                    }
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AnimalDetail.route,
            arguments = listOf(navArgument("microchip") { type = NavType.StringType })
        ) { backStackEntry ->
            val microchip = backStackEntry.arguments?.getString("microchip") ?: ""
            val uiState by animalViewModel.uiState.collectAsState()
            val animal = when (uiState) {
                is AnimalUiState.Success -> (uiState as AnimalUiState.Success).animales.find { it.microchip == microchip }
                else -> null
            }
            
            if (animal != null) {
                AnimalDetailScreen(
                    animal = animal,
                    viewModel = animalViewModel,
                    navController = navController,
                    onBack = { 
                        animalViewModel.fetchAnimales()
                        val popped = navController.popBackStack(Screen.Dashboard.route, false)
                        if (!popped) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    }
                )
            } else {
                LaunchedEffect(microchip) {
                    animalViewModel.searchByMicrochip(microchip)
                }
                Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        composable(
            route = Screen.MedicalHistory.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("nombre") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            MedicalHistoryScreen(
                idAnimal = id,
                nombreAnimal = nombre,
                userRole = "Vet", 
                viewModel = animalViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

---

## 5. Autenticación y Otros Componentes

### `app/src/main/java/com/safepaw/app/ui/viewmodels/AuthViewModel.kt`
```kotlin
package com.safepaw.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safepaw.app.data.models.UsuarioRegistro
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            supabaseClient.auth.sessionStatus.collect { status ->
                when (status) {
                    SessionStatus.LoadingFromStorage -> _authState.value = AuthState.Loading
                    is SessionStatus.Authenticated -> _authState.value = AuthState.Authenticated
                    is SessionStatus.NotAuthenticated -> _authState.value = AuthState.Idle
                    SessionStatus.NetworkError -> {
                        if (_authState.value !is AuthState.Authenticated) {
                            _authState.value = AuthState.Idle
                        }
                    }
                }
            }
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    password = pass
                }
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error de autenticación")
            }
        }
    }

    fun signUp(nombre: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = supabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    password = pass
                }
                val userId = user?.id
                    ?: supabaseClient.auth.currentSessionOrNull()?.user?.id
                    ?: supabaseClient.auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("No se pudo obtener el id del usuario")
                
                val codigoUsuario = (1..8).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")
                val usuarioRegistro = UsuarioRegistro(
                    id_usuario = userId,
                    nombre = nombre,
                    rol = "Voluntario",
                    mail = email,
                    contrasena = hashPassword(pass),
                    codigo_usuario = codigoUsuario
                )
                supabaseClient.postgrest["usuarios"].insert(usuarioRegistro)
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al crear la cuenta")
            }
        }
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun signOut() {
        viewModelScope.launch {
            supabaseClient.auth.signOut()
            _authState.value = AuthState.Idle
        }
    }
}
```

### `app/src/main/java/com/safepaw/app/ui/screens/LoginScreen.kt`
```kotlin
package com.safepaw.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.safepaw.app.ui.viewmodels.AuthViewModel
import com.safepaw.app.ui.viewmodels.AuthState

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (isRegisterMode) "Crear cuenta SafePaw" else "SafePaw Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        if (isRegisterMode) {
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (isRegisterMode) viewModel.signUp(nombre, email, password) else viewModel.signIn(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = (if (isRegisterMode) nombre.isNotEmpty() else true) && email.isNotEmpty() && password.isNotEmpty() && authState !is AuthState.Loading
        ) {
            Text(if (isRegisterMode) "Crear cuenta" else "Iniciar Sesión")
        }
        TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
            Text(if (isRegisterMode) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate")
        }
        if (authState is AuthState.Loading) CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        if (authState is AuthState.Error) Text(text = (authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        LaunchedEffect(authState) { if (authState is AuthState.Authenticated) onLoginSuccess() }
    }
}
```

### `app/src/main/java/com/safepaw/app/ui/screens/MedicalHistoryScreen.kt`
```kotlin
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
    userRole: String,
    viewModel: AnimalViewModel,
    onBack: () -> Unit
) {
    val tratamientos by viewModel.selectedAnimalTratamientos.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(idAnimal) { viewModel.fetchTratamientos(idAnimal) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial: $nombreAnimal") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") } }
            )
        },
        floatingActionButton = {
            if (userRole == "Vet" || userRole == "Gestor") {
                FloatingActionButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Añadir Tratamiento") }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (tratamientos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay tratamientos registrados") }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(tratamientos) { tratamiento -> TreatmentItem(tratamiento) }
                }
            }
        }
        if (showAddDialog) {
            AddTreatmentDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { tipo, desc, dur ->
                    viewModel.addTratamiento(Tratamiento(id_animal = idAnimal, tipo = tipo, descripcion = desc, fecha = java.time.LocalDateTime.now().toString(), duracion = dur))
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TreatmentItem(tratamiento: Tratamiento) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (tratamiento.tipo == "Intervención") Icons.Default.MedicalInformation else Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
    var desc by remember { mutableStateOf("") }
    var dur by remember { mutableStateOf("") }
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
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                OutlinedTextField(value = dur, onValueChange = { dur = it }, label = { Text("Duración") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
            }
        },
        confirmButton = { Button(onClick = { onConfirm(tipo, desc, dur) }, enabled = desc.isNotBlank()) { Text("Añadir") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
```

### `app/src/main/java/com/safepaw/app/di/NetworkModule.kt`
```kotlin
package com.safepaw.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://eiicceqdkngrynzvohyo.supabase.co",
            supabaseKey = "TU_API_KEY"
        ) {
            install(Postgrest)
            install(Auth)
            install(Storage)
        }
    }
}
```


### `app/src/main/java/com/safepaw/app/ui/screens/ScannerScreen.kt`
```kotlin
package com.safepaw.app.ui.screens

import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(onBarcodeDetected: (String) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var hasError by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    val imageAnalyzer = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                        .also { it.setAnalyzer(cameraExecutor) { imageProxy -> processImageProxy(imageProxy, onBarcodeDetected) } }
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
                    } catch (e: Exception) { hasError = true }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)) {
            if (hasError) Text("Error al iniciar la cámara", color = MaterialTheme.colorScheme.error)
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(imageProxy: ImageProxy, onBarcodeDetected: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        BarcodeScanning.getClient().process(image)
            .addOnSuccessListener { barcodes -> barcodes.forEach { it.rawValue?.let(onBarcodeDetected) } }
            .addOnCompleteListener { imageProxy.close() }
    } else imageProxy.close()
}
```

### `app/src/main/java/com/example/safepaw/MainActivity.kt`
```kotlin
package com.example.safepaw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.safepaw.ui.theme.SafePawTheme
import com.safepaw.app.ui.navigation.SafePawNavigation
import com.safepaw.app.ui.viewmodels.AnimalViewModel
import com.safepaw.app.ui.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafePawTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val animalViewModel: AnimalViewModel = hiltViewModel()
                SafePawNavigation(authViewModel = authViewModel, animalViewModel = animalViewModel)
            }
        }
    }
}
```

