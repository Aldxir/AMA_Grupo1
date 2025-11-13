package com.example.alden.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.alden.presentation.state.AppEvent
import com.example.alden.R
import kotlin.random.Random

class NotificationGatewayLocal(
    private val context: Context
) {
    private val nm = NotificationManagerCompat.from(context)
    //@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    //@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @SuppressLint("MissingPermission")
    fun handle(e: AppEvent.Notify) {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val id = e.channel.channelId() // <-- extensión top-level
        val notif = NotificationCompat.Builder(context, id)
            .setSmallIcon(R.drawable.ic_notification) // asegúrate que exista
            .setContentTitle(e.title)
            .setContentText(e.body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // < Android O
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        nm.notify(Random.nextInt(), notif)
    }

    //@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @SuppressLint("MissingPermission")
    private fun showNotify(e: AppEvent.Notify) {
        val channelId = when (e.channel) {
            NotificationChannelType.ASISTENCIA -> NotificationChannels.CH_ASISTENCIA
            NotificationChannelType.ALERTAS -> NotificationChannels.CH_ALERTAS
        }

        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(e.title)
            .setContentText(e.body)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(Random.nextInt(), notif)
    }
}