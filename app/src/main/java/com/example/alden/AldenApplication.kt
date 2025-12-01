package com.example.alden

import android.app.Application
import com.example.alden.di.Singletons
import com.example.alden.notifications.NotificationChannels // Importa tu objeto

class AldenApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Singletons.init(this)
        NotificationChannels.ensureCreated(this)
    }
}