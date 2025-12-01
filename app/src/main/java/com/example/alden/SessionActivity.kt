package com.example.alden

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.alden.data.UserRepository
import com.example.alden.di.Singletons
import com.example.alden.session.LoginType
import com.example.alden.ui.login.LoginActivity
import com.example.alden.ui.registro.RegistroActivity

class SessionActivity : AppCompatActivity() {
    // Handler para poder retrasar la navegación un poquito
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        // Espera 600 ms para que se vea el ProgressBar de activity_session
        handler.postDelayed({
            decidirDestino()
        }, 400L)

    }

    private fun decidirDestino() {
        val session = Singletons.session.getSessionInfo()

        if (!session.isLoggedIn) {
            // No hay sesión → ir a login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Hay sesión activa
        val intent = Intent(this, RegistroActivity::class.java)

        if (session.loginType == LoginType.LOCAL) {
            // Sesión LOCAL → reconstruimos el usuario desde UserRepository usando el email
            val email = session.email
            val usuarioLocal = email?.let { UserRepository.findByCorreo(it) }

            if (usuarioLocal != null) {
                // Mandamos los mismos extras que en LoginActivity
                intent.putExtra(LoginActivity.EXTRA_USER_ID, usuarioLocal.id)
                intent.putExtra(LoginActivity.EXTRA_USER_NAME, usuarioLocal.nombre)
                intent.putExtra(LoginActivity.EXTRA_USER_ROL, usuarioLocal.rol.name)
            } else {
                // Por si algo sale mal, limpiamos sesión y mandamos a login
                Singletons.session.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return
            }
        }

        startActivity(intent)
        finish()
    }
}