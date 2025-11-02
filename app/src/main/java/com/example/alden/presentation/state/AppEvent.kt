package com.example.alden.presentation.state

sealed interface AppEvent {
    // Evento para notificación local (NotificationCompat)
    data class Notify(
        val title: String,
        val body: String,
        val channel: NotificationChannelType
    ) : AppEvent

    // Evento efímero para UI (Toast/Snackbar)
    data class ShowToast(val message: String) : AppEvent
}

// Canales lógicos
enum class NotificationChannelType { ASISTENCIA, ALERTAS }