package com.safepaw.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safepaw.app.data.models.Animal
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
            _uiState.value = AnimalUiState.Loading
            try {
                val animal = repository.getAnimalByMicrochip(microchip)
                if (animal != null) {
                    _uiState.value = AnimalUiState.Success(listOf(animal))
                } else {
                    _uiState.value = AnimalUiState.Error("No se encontró ningún animal con ese microchip")
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
                // Manejar error de carga de tratamientos
            }
        }
    }

    fun upsertAnimal(animal: Animal) {
        viewModelScope.launch {
            try {
                repository.insertAnimal(animal)
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
                // Manejar error
            }
        }
    }

    fun uploadPhoto(idAnimal: String, bytes: ByteArray, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val url = repository.uploadAnimalPhoto(idAnimal, bytes)
                onComplete(url)
            } catch (e: Exception) {
                // Manejar error
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
