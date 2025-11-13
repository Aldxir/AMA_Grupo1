package com.example.alden.flow.location

import com.example.alden.models.Ubicacion
import kotlinx.coroutines.flow.StateFlow

interface LocationSource {
    // Ubicacion actual DENTRO_RANGO | FUERA_RANGO
    val zoneFlow: StateFlow<Ubicacion>

    // Cambiar la zona
    suspend fun setZone(zone: Ubicacion)
}