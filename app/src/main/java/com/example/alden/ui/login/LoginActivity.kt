package com.example.alden.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.alden.R
import com.example.alden.data.UserRepository
import com.example.alden.ui.registro.RegistroActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvMensaje: TextView

    companion object {
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_USER_NAME = "user_name"
        const val EXTRA_USER_ROL = "user_rol"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        btnLogin = findViewById(R.id.btnLogin)
        tvMensaje = findViewById(R.id.tvMensaje)

        btnLogin.setOnClickListener { intentarLogin() }
        etContrasena.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                intentarLogin(); true
            } else false
        }
    }

    private fun intentarLogin() {
        val emailInput = etCorreo.text.toString().trim()
        val passInput = etContrasena.text.toString().trim()

        if (emailInput.isEmpty()) {
            tvMensaje.text = "Ingresa tu correo."
            etCorreo.requestFocus(); return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            tvMensaje.text = "Formato de correo no válido."
            etCorreo.requestFocus(); return
        }
        if (passInput.isEmpty()) {
            tvMensaje.text = "Ingresa tu contraseña."
            etContrasena.requestFocus(); return
        }

        val usuarioAutenticado = UserRepository.autenticarPorCorreo(emailInput, passInput)
        if (usuarioAutenticado != null) {
            val intent = Intent(this, RegistroActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, usuarioAutenticado.id)          // id interno
                putExtra(EXTRA_USER_NAME, usuarioAutenticado.nombre)
                putExtra(EXTRA_USER_ROL, usuarioAutenticado.rol.name)
            }
            startActivity(intent)
            finish()
        } else {
            val user = UserRepository.findByCorreo(emailInput)
            tvMensaje.text = when {
                user == null -> "Correo no encontrado."
                !user.enabled -> "Usuario deshabilitado. Contacta al administrador."
                else -> "Contraseña incorrecta."
            }
        }
    }
}