package com.example.alden.flow.auth

import com.example.alden.models.Usuario
import kotlinx.coroutines.flow.StateFlow

interface AuthSource {
    // Usuario actual, null si no ay login
    val userFlow: StateFlow<Usuario?>

    // Login simulado como un usuario especifico
    suspend fun loginAs(user: Usuario)

    // Logout simulado
    suspend fun logout()
}