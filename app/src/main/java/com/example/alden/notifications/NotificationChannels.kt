package com.example.alden.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build


enum class NotificationChannelType { ASISTENCIA, ALERTAS }

fun NotificationChannelType.channelId(): String = when (this) {
    NotificationChannelType.ASISTENCIA -> NotificationChannels.CH_ASISTENCIA
    NotificationChannelType.ALERTAS    -> NotificationChannels.CH_ALERTAS
}
object NotificationChannels {
    const val CH_ASISTENCIA = "asistencia_v2"
    const val CH_ALERTAS = "alertas_v2"
    const val CH_DEFAULT = "default_v2"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            nm.createNotificationChannel(
                NotificationChannel(CH_ASISTENCIA, "Asistencia",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Notificaciones de asistencia" }
            )

            nm.createNotificationChannel(
                NotificationChannel(CH_ALERTAS, "Alertas",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Bloqueos por horario/rango"
                    enableVibration(true)
                }
            )
            nm.createNotificationChannel(
                NotificationChannel(CH_DEFAULT, "General",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones generales"
                }
            )
        }
    }
}