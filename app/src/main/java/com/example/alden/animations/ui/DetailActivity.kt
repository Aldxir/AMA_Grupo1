package com.example.alden.animations.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.alden.R
import com.example.alden.accesscontrol.ScreenGuard
import com.example.alden.animations.effects.ViewAnimations
import com.example.alden.animations.transitions.TransitionNavigator
import com.example.alden.auth.AuthSession
import com.example.alden.auth.GoogleAuthMapper
import com.example.alden.data.UserRepository
import com.example.alden.di.Singletons
import com.example.alden.models.Usuario
import com.example.alden.ui.login.LoginActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla simple de "Detalle" para demostrar transiciones Dashboard → Detalle.
 * Muestra el último estado persistido (Práctica 11).
 */
class DetailActivity : AppCompatActivity() {

    private var currentUser: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        currentUser = resolveUser()
        if (!ScreenGuard.validateSession(this, currentUser)) return

        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvBody = findViewById<TextView>(R.id.tvDetailBody)

        val state = Singletons.animStateStore.read()

        tvTitle.text = "Detalle del usuario"
        tvBody.text = buildString {
            append("Usuario: ")
            append(currentUser?.nombre ?: "-")
            append("\nRol: ")
            append(currentUser?.rol?.name ?: "-")
            append("\n\nÚltima acción: ")
            append(state.lastAction ?: "(ninguna)")
            append("\nÉxito: ")
            append(if (state.lastRegisterSuccess) "Sí" else "No")
            if (state.lastActionTimestamp > 0L) {
                val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                append("\nFecha: ")
                append(fmt.format(Date(state.lastActionTimestamp)))
            }
        }

        ViewAnimations.enter(findViewById(R.id.cardDetail))

        val session = Singletons.session.getSessionInfo()
        if (!session.isLoggedIn) {
            ScreenGuard.redirectToLogin(this)
            return
        }

    }

    override fun onResume() {
        super.onResume()
        if (!Singletons.session.getSessionInfo().isLoggedIn) {
            ScreenGuard.redirectToLogin(this)
            return
        }
    }

    override fun finish() {
        super.finish()
        val type = TransitionNavigator.run { readTransitionType() }
        TransitionNavigator.run { applyFinishTransition(type) }
    }

    private fun resolveUser(): Usuario? {
        val extraUserId = intent.getStringExtra(LoginActivity.EXTRA_USER_ID)
        val firebaseUser = AuthSession.currentUser
        val usuarioLocal = extraUserId?.let { UserRepository.findById(it) }
        return usuarioLocal ?: firebaseUser?.let { GoogleAuthMapper.toUsuario(it) }
    }

}
