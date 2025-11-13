package com.example.alden.firebase

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.alden.R
import com.example.alden.notifications.NotificationChannels
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.atomic.AtomicInteger

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFCM"
        private val notificationIdGenerator = AtomicInteger()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Mensaje recibido de: ${message.from}")

        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Data Payload: ${message.data}")

            val data = message.data
            val title = data["title"] ?: "Mensaje de Alden"
            val body = data["body"] ?: "Contenido no especificado"

            // Decidir el canal de notificación
            val channelType = data["channel"] // "asistencia", "alerta", etc.

            // *** ESTE ES EL CAMBIO CLAVE ***
            // Usamos tus constantes de NotificationChannels
            val channelId = when (channelType) {
                "asistencia" -> NotificationChannels.CH_ASISTENCIA
                "alerta"     -> NotificationChannels.CH_ALERTAS
                else         -> NotificationChannels.CH_DEFAULT // Fallback al canal por defecto
            }

            Log.d(TAG, "Mostrando notificación en canal: $channelId")
            sendLocalNotification(title, body, channelId)

        } else {
            // Manejar mensajes de "notificación" en primer plano
            message.notification?.let {
                sendLocalNotification(
                    it.title ?: "Notificación",
                    it.body ?: "Cuerpo",
                    NotificationChannels.CH_DEFAULT // Usar canal por defecto
                )
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM registrado: $token")
        // sendTokenToYourBackend(token)
    }

    private fun sendLocalNotification(title: String, body: String, channelId: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_default)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val uniqueId = notificationIdGenerator.getAndIncrement()
        notificationManager.notify(uniqueId, notificationBuilder.build())
    }
}