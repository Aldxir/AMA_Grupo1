package com.example.alden.flow.attendance

import com.example.alden.models.RegistroAcceso
import kotlinx.coroutines.flow.StateFlow

interface AttendanceRepository {
    // Registros en memoria (o cache/local DB)
    val recordsFlow: StateFlow<List<RegistroAcceso>>

    // Agrega un registro (si la UI/Policy lo permite)
    suspend fun add(registro: RegistroAcceso)

    // Limpia todos los registros (solo para pruebas)
    suspend fun clear()
}