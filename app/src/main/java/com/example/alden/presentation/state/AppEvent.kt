package com.example.alden.presentation.state

import com.example.alden.notifications.NotificationChannelType

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
