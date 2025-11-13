package com.example.alden

import android.app.Application
import com.example.alden.notifications.NotificationChannels // Importa tu objeto

class AldenApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureCreated(this)
    }
}