package com.example.alden.flow.time

import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime

interface TimeSource {
    // Hora del sistema
    val timeFlow: StateFlow<LocalTime>

    // Iniciar el ticker (por ejemplo, cada 60_000 ms)
    fun start(tickMillis: Long = 60_000L)

    // Detener el ticker
    fun stop()
}