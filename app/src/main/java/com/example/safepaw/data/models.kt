package com.example.safepaw.data

import java.util.UUID

data class Animal(
    val id_animal: UUID = UUID.randomUUID(),
    val microchip: String,
    val nombre: String,
    val especie: String,
    val estado_adopcion: String
)

data class Tratamiento(
    val id_tratamiento: UUID = UUID.randomUUID(),
    val id_animal: UUID,
    val tipo: String,
    val fecha: Long
)

data class Usuario(
    val id_usuario: UUID,
    val nombre: String,
    val rol: String
)

data class Adopcion(
    val id_adopcion: UUID = UUID.randomUUID(),
    val id_animal: UUID,
    val id_usuario: UUID,
    val estado: String
)
