package com.example.safepaw.data

import com.example.safepaw.data.Animal
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class SupabaseRepository(private val postgrest: Postgrest) {

    suspend fun getAnimal(id: UUID): Animal? = withContext(Dispatchers.IO) {
        postgrest.from("animales").select {
            filter {
                eq("id_animal", id.toString())
            }
        }.decodeSingleOrNull()
    }

    suspend fun getAnimals(): List<Animal> = withContext(Dispatchers.IO) {
        postgrest.from("animales").select().decodeList()
    }

    suspend fun insertAnimal(animal: Animal) = withContext(Dispatchers.IO) {
        postgrest.from("animales").insert(animal)
    }

    suspend fun updateAnimal(animal: Animal) = withContext(Dispatchers.IO) {
        postgrest.from("animales").update({
            set("microchip", animal.microchip)
            set("nombre", animal.nombre)
            set("especie", animal.especie)
            set("estado_adopcion", animal.estado_adopcion)
        }) {
            filter {
                eq("id_animal", animal.id_animal.toString())
            }
        }
    }

    suspend fun deleteAnimal(id: UUID) = withContext(Dispatchers.IO) {
        postgrest.from("animales").delete {
            filter {
                eq("id_animal", id.toString())
            }
        }
    }
}