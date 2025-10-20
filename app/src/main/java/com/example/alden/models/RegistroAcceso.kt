package com.example.alden.models

import java.time.LocalDateTime

data class RegistroAcceso(
    val usuario: Usuario,
    val accion: Accion,
    val ubicacion: Ubicacion,
    val timestamp: LocalDateTime
)
