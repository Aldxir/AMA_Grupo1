package com.example.alden.rules

import com.example.alden.models.RegistroAcceso

// Politica final: AND de las tres reglas atomicas
val canRegister: Predicate<RegistroAcceso> =
    usuarioHabilitado and horarioValido and ubicacionValida