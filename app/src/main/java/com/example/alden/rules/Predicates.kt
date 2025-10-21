package com.example.alden.rules

import com.example.alden.models.RegistroAcceso
import com.example.alden.models.Ubicacion

// Reglas atomicas
val usuarioHabilitado: Predicate<RegistroAcceso> = { it.usuario.enabled}
val horarioValido: Predicate<RegistroAcceso> = { it.hora.allowedAt() }
val ubicacionValida: Predicate<RegistroAcceso> = { it.ubicacion == Ubicacion.DENTRO_RANGO }
