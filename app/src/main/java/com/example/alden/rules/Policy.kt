package com.example.alden.rules

// Politica final: AND de las tres reglas atomicas
val canRegister: Predicate<PolicyInput> =
    usuarioHabilitado and horarioValido and ubicacionValida