package com.safepaw.app.data.repository

import com.safepaw.app.data.models.Animal
import com.safepaw.app.data.models.Tratamiento
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
