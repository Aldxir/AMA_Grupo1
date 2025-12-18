package com.example.alden.ui.registro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alden.R
import com.example.alden.accesscontrol.ScreenGuard // [IMPORTANTE] Nuevo módulo de seguridad
import com.example.alden.auth.AuthSession
import com.example.alden.auth.GoogleAuthMapper
import com.example.alden.data.UserRepository
import com.example.alden.di.Singletons
import com.example.alden.flow.time.TimeSource
import com.example.alden.models.Accion
import com.example.alden.models.Rol
import com.example.alden.models.Ubicacion
import com.example.alden.models.Usuario
import com.example.alden.presentation.MainViewModel
import com.example.alden.presentation.MainViewModelFactory
import com.example.alden.presentation.state.AppEvent
import com.example.alden.ui.RegistroAdapter
import com.example.alden.ui.login.LoginActivity
import kotlinx.coroutines.launch
import com.example.alden.ui.rating.RatingActivity

class RegistroActivity : AppCompatActivity() {

    private val time: TimeSource = Singletons.time
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnCerrarSesion: Button
    private var imgUserPhoto: ImageView? = null

    // Creamos las mismas dependencias que en MainActivity y construimos el ViewModel
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            Singletons.auth,
            Singletons.location,
            Singletons.time,
            Singletons.attendance,
            Singletons.policy
        )
    }

    private lateinit var adapter: RegistroAdapter
    private var updatingFromVm = false  // evita loops entre VM y UI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Referencias a las vistas de usuario
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        imgUserPhoto = findViewById(R.id.imgUserPhoto)
        val swZona = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.swZona)
        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        val tvEstado = findViewById<TextView>(R.id.tvEstado)
        val btnEntrada = findViewById<Button>(R.id.btnEntrada)
        val btnSalida = findViewById<Button>(R.id.btnSalida)

        // 1) Intentar leer los extras del login clásico
        val extraUserId   = intent.getStringExtra(LoginActivity.EXTRA_USER_ID)
        val extraUserName = intent.getStringExtra(LoginActivity.EXTRA_USER_NAME)
        // val extraUserRol  = intent.getStringExtra(LoginActivity.EXTRA_USER_ROL) // No estrictamente necesario si recuperamos el obj Usuario

        // 2) Leer el usuario de Firebase (para login con Google)
        val firebaseUser = AuthSession.currentUser

        // 3) Validar sesión básica inicial (Redundancia de seguridad)
        if (extraUserId == null && firebaseUser == null) {
            // Si falla la lógica manual, ScreenGuard lo atrapará más abajo, pero esto es un fail-safe rápido.
            ScreenGuard.redirectToLogin(this)
            return
        }

        // 4) UI: Decidir qué nombre/correo mostrar
        val nombreMostrado = when {
            extraUserName != null -> extraUserName
            firebaseUser?.displayName != null -> firebaseUser.displayName
            else -> "Usuario"
        }
        val correoMostrado = firebaseUser?.email ?: "Sin correo"

        tvUserName.text = nombreMostrado
        tvUserEmail.text = correoMostrado

        // 5) Foto de perfil (solo si viene de Google y hay photoUrl)
        val photoUrl = firebaseUser?.photoUrl
        if (photoUrl != null && imgUserPhoto != null) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.ic_person_background)
                .circleCrop()
                .into(imgUserPhoto!!)
        }

        // =================================================================
        // LÓGICA DE USUARIO Y SEGURIDAD (PRÁCTICA 08)
        // =================================================================

        // Variable para almacenar el objeto Usuario de dominio final
        var usuarioFinal: Usuario? = null

        // A) Recuperar usuario del Intent (Login Local)
        val userId = intent.getStringExtra(LoginActivity.EXTRA_USER_ID)
        val usuarioLocal = userId?.let { UserRepository.findById(it) }

        if (usuarioLocal != null) {
            usuarioFinal = usuarioLocal
            tvHeader.text = "${usuarioLocal.nombre} (${usuarioLocal.rol.name})"
        }
        // B) Recuperar usuario de Firebase (Login Google)
        else if (firebaseUser != null) {
            val usuarioGoogle = GoogleAuthMapper.toUsuario(firebaseUser)
            usuarioFinal = usuarioGoogle
            tvHeader.text = "${usuarioGoogle.nombre} (${usuarioGoogle.rol.name})"
        } else {
            tvHeader.text = "Sesión desconocida"
        }

        // --- INICIO IMPLEMENTACIÓN ACCESSCONTROL ---

        // 1. Validar Sesión con ScreenGuard
        // Si usuarioFinal es null, redirige a Login y mata la activity
        if (!ScreenGuard.validateSession(this, usuarioFinal)) {
            return
        }

        // 2. Aplicar Protección Visual (FLAG_SECURE) si es necesario (Admin)
        ScreenGuard.applyVisualProtection(this, usuarioFinal)

        // 3. Restricción de UI por Rol
        // Si es Admin, deshabilitamos botones de registro (solo visualización)
        if (usuarioFinal?.rol == Rol.ADMIN) {
            btnEntrada.isEnabled = false
            btnSalida.isEnabled = false
            btnEntrada.alpha = 0.5f // Efecto visual de deshabilitado
            btnSalida.alpha = 0.5f
        }

        // --- FIN IMPLEMENTACIÓN ACCESSCONTROL ---

        // Notificar al ViewModel quién es el usuario actual para que inicie los flujos
        usuarioFinal?.let { viewModel.loginAsAdminOrUser(it) }


        // Configuración RecyclerView
        adapter = RegistroAdapter()
        findViewById<RecyclerView>(R.id.rvRegistros).apply {
            layoutManager = LinearLayoutManager(this@RegistroActivity)
            adapter = this@RegistroActivity.adapter
        }

        // Listeners de botones
        btnEntrada.setOnClickListener {
            viewModel.registrar(Accion.ENTRADA)
        }
        btnSalida.setOnClickListener {
            viewModel.registrar(Accion.SALIDA)
        }

        // Listener: usuario mueve el switch -> actualiza zona en VM
        swZona.setOnCheckedChangeListener { _, checked ->
            if (updatingFromVm) return@setOnCheckedChangeListener
            viewModel.setZone(if (checked) Ubicacion.DENTRO_RANGO else Ubicacion.FUERA_RANGO)
        }

        // Listener Cerrar Sesión (Cumple requisito de limpiar Back Stack)
        btnCerrarSesion.setOnClickListener {
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()

            // 1) Cerrar sesión en SessionManager
            Singletons.session.logout()

            // 2) Ir a LoginActivity y limpiar el back stack
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        // 3) Observa el estado agregado y los eventos del ViewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.appState.collect { st ->
                        tvEstado.text = st.mensajeEstado
                        // st.registros ya viene filtrado por rol desde el VM
                        adapter.submit(st.registros)
                    }
                }
                launch {
                    viewModel.events.collect { ev ->
                        when (ev) {
                            is AppEvent.ShowToast ->
                                Toast.makeText(this@RegistroActivity, ev.message, Toast.LENGTH_SHORT).show()
                            is AppEvent.Notify -> {
                                Toast.makeText(this@RegistroActivity, "${ev.title}: ${ev.body}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.appState.collect { st ->
                        // Actualiza texto y posición del switch según la VM (sin disparar listener)
                        updatingFromVm = true
                        val dentro = (st.ubicacion == Ubicacion.DENTRO_RANGO)
                        if (swZona.isChecked != dentro) swZona.isChecked = dentro
                        swZona.text = if (dentro) "Dentro de rango" else "Fuera de rango"
                        updatingFromVm = false

                        // NOTA: La lógica de habilitar botones ahora se maneja arriba con el Rol (ScreenGuard/Lógica manual)
                        // y con el estado 'puedeRegistrar' si quisieras feedback en tiempo real.
                    }
                }
            }
        }
        findViewById<Button>(R.id.btnIrRating).setOnClickListener {
            val intent = Intent(this, RatingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun verificarSesionActiva() {
        // Doble chequeo usando SessionManager, útil si la app se reanuda después de mucho tiempo
        val session = Singletons.session.getSessionInfo()
        if (!session.isLoggedIn) {
            ScreenGuard.redirectToLogin(this)
        }
    }

    override fun onStart() {
        super.onStart()
        verificarSesionActiva()
        time.start(1000L)
    }

    override fun onStop() {
        viewModel.stopClock()
        super.onStop()
    }
}