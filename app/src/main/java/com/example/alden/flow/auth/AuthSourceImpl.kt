package com.example.alden.flow.auth

import com.example.alden.models.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthSourceImpl(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : AuthSource {
    private val _user = MutableStateFlow<Usuario?>(null)
    override val userFlow: StateFlow<Usuario?> = _user.asStateFlow()

    override suspend fun loginAs(user: Usuario) {
        _user.emit(user)
    }

    override suspend fun logout() {
        _user.emit(null)
    }

    // Helpers opcionales para ejecutar desde UI sin bloquear
    //fun loginAsync(user: Usuario) = scope.launch { loginAs(user) }
    //fun logoutAsync() = scope.launch { logout() }
}
