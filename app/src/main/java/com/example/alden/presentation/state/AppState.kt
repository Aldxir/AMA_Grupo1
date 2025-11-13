package com.example.alden.presentation.state

import com.example.alden.models.RegistroAcceso
import com.example.alden.models.Ubicacion
import com.example.alden.models.Usuario
import java.time.LocalTime

data class AppState(
    val usuario: Usuario?,
    val ubicacion: Ubicacion,
    val horaActual: LocalTime,
    val puedeRegistrar: Boolean,
    val registros: List<RegistroAcceso>,
    val mensajeEstado: String, // "HABILITADO" | "DESHABILITADO"
)
