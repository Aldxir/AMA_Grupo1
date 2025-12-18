package com.example.alden.accesscontrol

import android.app.Activity
import android.content.Intent
import android.view.WindowManager
import android.widget.Toast
import com.example.alden.models.Rol
import com.example.alden.models.Usuario
import com.example.alden.ui.login.LoginActivity

/**
 * Clase encargada de la protección de pantallas y seguridad visual.
 * Cumple con los objetivos de la Práctica 08.
 */
object ScreenGuard {

    /**
     * Valida si el usuario tiene permiso para estar en esta pantalla.
     * Si no tiene sesión, lo manda al Login[cite: 23, 57].
     */
    fun validateSession(activity: Activity, user: Usuario?): Boolean {
        if (user == null) {
            redirectToLogin(activity)
            return false
        }
        return true
    }

    /**
     * Aplica protección visual (FLAG_SECURE) si la pantalla muestra datos sensibles.
     * Requerimiento: Pantalla de registros del Admin.
     */
    fun applyVisualProtection(activity: Activity, user: Usuario?) {
        if (user?.rol == Rol.ADMIN) {
            // Evita capturas de pantalla y visualización en apps recientes
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            // Para usuarios normales (User) permitimos capturas (opcional, o se limpia la flag)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    /**
     * Redirige al Login limpiando el Back Stack para que no se pueda volver atrás.
     */
    fun redirectToLogin(activity: Activity) {
        Toast.makeText(activity, "Acceso denegado: Inicie sesión", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
        activity.finish()
    }
}