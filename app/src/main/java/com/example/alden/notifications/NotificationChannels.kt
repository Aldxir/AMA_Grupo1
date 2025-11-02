package com.example.alden.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val CH_ASISTENCIA = "asistencia"
    const val CH_ALERTAS = "alertas"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val asistencia = NotificationChannel(
            CH_ASISTENCIA, "Asistencia", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Registros válidos de asistencia" }

        val alertas = NotificationChannel(
            CH_ALERTAS, "Alertas", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Fuera de horario o fuera de zona" }

        nm.createNotificationChannel(asistencia)
        nm.createNotificationChannel(alertas)
    }
}