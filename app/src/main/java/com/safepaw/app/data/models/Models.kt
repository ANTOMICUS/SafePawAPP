package com.safepaw.app.data.models

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Animal(
    val id_animal: String = UUID.randomUUID().toString(),
    val microchip: String,
    val nombre: String,
    val especie: String,
    val estado_adopcion: String
)

@Serializable
data class Tratamiento(
    val id_tratamiento: String = UUID.randomUUID().toString(),
    val id_animal: String,
    val tipo: String,
    val fecha: String // ISO 8601
)

@Serializable
data class Usuario(
    val id_usuario: String,
    val nombre: String,
    val rol: String // Gestor, Voluntario, Vet
)

@Serializable
data class Adopcion(
    val id_adopcion: String = UUID.randomUUID().toString(),
    val id_animal: String,
    val id_usuario: String,
    val estado: String
)
