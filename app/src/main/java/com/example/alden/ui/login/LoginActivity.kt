package com.example.alden.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.alden.R
import com.example.alden.data.UserRepository
import com.example.alden.ui.registro.RegistroActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvMensaje: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var btnGoogle: SignInButton

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
        btnGoogle = findViewById(R.id.btnGoogleSignIn)

        // FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnLogin.setOnClickListener { intentarLogin() }
        etContrasena.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                intentarLogin(); true
            } else false
        }

        // Login con Google
        btnGoogle.setOnClickListener {
            tvMensaje.text = "Click en botón de Google..."
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
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

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account)
            } else {
                tvMensaje.text = "No se pudo obtener la cuenta de Google."
            }
        } catch (e: ApiException) {
            val code = e.statusCode
            tvMensaje.text = "Error Google (código $code)"
            android.util.Log.e("LoginActivity", "Google sign in failed, code=$code", e)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login con Google correcto → ir a RegistroActivity (pantalla principal)
                    val intent = Intent(this, RegistroActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    tvMensaje.text = "Error autenticando con Firebase."
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Ya hay sesión activa (por Google)
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}