package com.example.alden.session

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SessionManager(
    private val context: Context,
    private val auth: FirebaseAuth
) {
    // Nombre del archivo de SharedPreferences
    private val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    // Claves que se usaran en SharedPreferences
    private companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TYPE = "login_type"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_PHOTO_URL = "photo_url"
    }

    /**
     * Devuelve el estado de sesión actual combinando:
     * - FirebaseAuth.currentUser (Google / email)
     * - Datos guardados en SharedPreferences (LOCAL)
     */
    fun getSessionInfo(): SessionInfo {
        val firebaseUser = auth.currentUser

        // 1) Si hay usuario en Firebase, esa es nuestra verdad para sesión GOOGLE
        if (firebaseUser != null) {
            return buildSessionFromFirebase(firebaseUser)
        }

        // 2) Si no hay usuario en Firebase, miramos la sesión LOCAL en SharedPreferences
        val isLoggedInLocal = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedInLocal) {
            // No hay sesión guardada
            return SessionInfo(
                isLoggedIn = false,
                loginType = null,
                userId = null,
                email = null,
                displayName = null,
                photoUrl = null
            )
        }

        // Hay sesión local guardada
        val loginTypeString = prefs.getString(KEY_LOGIN_TYPE, null)
        val loginType = loginTypeString?.let {
            try {
                LoginType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        // Si el tipo de login guardado no es LOCAL, consideramos que la sesión es inválida
        if (loginType != LoginType.LOCAL) {
            // Sé estricto: limpiamos basura por si acaso
            clearPrefs()
            return SessionInfo(
                isLoggedIn = false,
                loginType = null,
                userId = null,
                email = null,
                displayName = null,
                photoUrl = null
            )
        }

        val userId = prefs.getString(KEY_USER_ID, null)
        val email = prefs.getString(KEY_EMAIL, null)
        val displayName = prefs.getString(KEY_DISPLAY_NAME, null)
        val photoUrl = prefs.getString(KEY_PHOTO_URL, null)

        return SessionInfo(
            isLoggedIn = true,
            loginType = LoginType.LOCAL,
            userId = userId,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl
        )
    }

    /**
     * Construye un SessionInfo a partir de un FirebaseUser (login con Google / email).
     * IMPORTANTE: aunque no hayamos llamado a onGoogleLoginSuccess, esto permite
     * reconstruir la sesión solo desde FirebaseAuth.
     */
    private fun buildSessionFromFirebase(user: FirebaseUser): SessionInfo {
        return SessionInfo(
            isLoggedIn = true,
            loginType = LoginType.GOOGLE,   // asumimos Google para tu caso
            userId = user.uid,
            email = user.email,
            displayName = user.displayName,
            photoUrl = user.photoUrl?.toString()
        )
    }

    /**
     * Llamar después de un login exitoso con Google/FirebaseAuth.
     */
    fun onGoogleLoginSuccess(user: FirebaseUser) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_LOGIN_TYPE, LoginType.GOOGLE.name)
            .putString(KEY_USER_ID, user.uid)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_PHOTO_URL, user.photoUrl?.toString())
            .apply()
    }

    /**
     * Llamar después de un login exitoso usando UsuarioRepository
     * (usuarios locales).
     */
    fun onLocalLoginSuccess(
        userId: String,
        email: String?,
        displayName: String?
    ) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_LOGIN_TYPE, LoginType.LOCAL.name)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_DISPLAY_NAME, displayName)
            .putString(KEY_PHOTO_URL, null) // normalmente no tienes foto local
            .apply()
    }

    /**
     * Decide si al arrancar la app se debe ir a LOGIN o a MAIN.
     */
    fun shouldGoToMainScreen(): Boolean {
        val session = getSessionInfo()
        return session.isLoggedIn
    }

    /**
     * Cierra la sesión:
     * - Google/Firebase si aplica
     * - Limpia SharedPreferences
     */
    fun logout() {
        // Si hay usuario en Firebase (Google u otro método), cerramos sesión en Firebase
        if (auth.currentUser != null) {
            auth.signOut()
        }

        // En cualquier caso limpiamos nuestra sesión local
        clearPrefs()
    }

    /**
     * Limpia todas las claves de sesión en SharedPreferences.
     */
    private fun clearPrefs() {
        prefs.edit()
            .clear()
            .apply()
    }
}