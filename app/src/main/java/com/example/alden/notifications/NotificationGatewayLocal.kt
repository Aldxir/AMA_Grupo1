package com.example.alden.notifications
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.alden.presentation.state.AppEvent
import com.example.alden.presentation.state.NotificationChannelType
import com.example.alden.R
import kotlin.random.Random

class NotificationGatewayLocal(
    private val context: Context
) {
    //@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun handle(event: AppEvent) {
        when (event) {
            is AppEvent.Notify -> showNotify(event)
            is AppEvent.ShowToast -> { /* el Toast lo manejas en la Activity */ }
        }
    }

    //@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @SuppressLint("MissingPermission")
    private fun showNotify(e: AppEvent.Notify) {
        val channelId = when (e.channel) {
            NotificationChannelType.ASISTENCIA -> NotificationChannels.CH_ASISTENCIA
            NotificationChannelType.ALERTAS -> NotificationChannels.CH_ALERTAS
        }

        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // pon un ícono existente del proyecto
            .setContentTitle(e.title)
            .setContentText(e.body)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(Random.nextInt(), notif)
    }
}