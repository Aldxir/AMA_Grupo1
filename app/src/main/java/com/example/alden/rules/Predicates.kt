package com.example.alden.rules

import com.example.alden.models.Ubicacion
import com.example.alden.models.Usuario
import java.time.LocalTime

data class PolicyInput(
    val usuario: Usuario,
    val hora: LocalTime,
    val ubicacion: Ubicacion
)

// Reglas atomicas
val usuarioHabilitado: Predicate<PolicyInput> = { it.usuario.enabled}
val horarioValido: Predicate<PolicyInput> = { it.hora.allowedAt() }
val ubicacionValida: Predicate<PolicyInput> = { it.ubicacion == Ubicacion.DENTRO_RANGO }
