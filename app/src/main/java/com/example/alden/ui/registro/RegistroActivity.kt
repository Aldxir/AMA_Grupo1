package com.example.alden.ui.registro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.alden.R
import com.example.alden.ui.login.LoginActivity
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alden.ui.RegistroAdapter
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.Toast
import com.example.alden.data.UserRepository
import com.example.alden.models.Accion
import com.example.alden.models.Ubicacion
import com.example.alden.presentation.MainViewModel
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.alden.di.Singletons
import com.example.alden.flow.time.TimeSource
import com.example.alden.presentation.MainViewModelFactory
import com.example.alden.presentation.state.AppEvent
import kotlinx.coroutines.launch
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.alden.auth.AuthSession
import com.example.alden.auth.GoogleAuthMapper



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

        // 1) Intentar leer los extras del login clásico
        val extraUserId   = intent.getStringExtra(LoginActivity.EXTRA_USER_ID)
        val extraUserName = intent.getStringExtra(LoginActivity.EXTRA_USER_NAME)
        val extraUserRol  = intent.getStringExtra(LoginActivity.EXTRA_USER_ROL)

        // 2) Leer el usuario de Firebase (para login con Google)
        val firebaseUser = AuthSession.currentUser

        // 3) Validar sesión:
        //    - Si NO hay extras y TAMPOCO hay usuario de Firebase -> sesión inválida
        if (extraUserId == null && firebaseUser == null) {
            Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 4) Decidir qué nombre/correo mostrar
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

        btnCerrarSesion.setOnClickListener {
            // Mensaje de confirmación
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()

            // 1) Cerrar sesión en SessionManager (FirebaseAuth + prefs locales)
            Singletons.session.logout()

            // 2) Ir a LoginActivity y limpiar el back stack
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            startActivity(intent)
            finish()
        }

        val swZona = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.swZona)


        // 1) Recuperar usuario del Intent (solo login clásico)
        val userId = intent.getStringExtra(LoginActivity.EXTRA_USER_ID)
        val usuario = userId?.let { UserRepository.findById(it) }

        // 2) UI
        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        val tvEstado = findViewById<TextView>(R.id.tvEstado)

        if (usuario != null) {
            // Caso login clásico con UserRepository
            viewModel.loginAsAdminOrUser(usuario)
            tvHeader.text = "${usuario.nombre} (${usuario.rol.name})"
        } else if (firebaseUser != null) {
            // Caso login con Google: crear Usuario equivalente
            val usuarioGoogle = GoogleAuthMapper.toUsuario(firebaseUser)

            // Loguear también en el ViewModel para habilitar registros de asistencia
            viewModel.loginAsAdminOrUser(usuarioGoogle)

            tvHeader.text = "${usuarioGoogle.nombre} (${usuarioGoogle.rol.name})"

        } else {
            tvHeader.text = "Sesión desconocida"
        }

        adapter = RegistroAdapter()
        findViewById<RecyclerView>(R.id.rvRegistros).apply {
            layoutManager = LinearLayoutManager(this@RegistroActivity)
            adapter = this@RegistroActivity.adapter
        }

        findViewById<Button>(R.id.btnEntrada).setOnClickListener {
            viewModel.registrar(Accion.ENTRADA)
        }
        findViewById<Button>(R.id.btnSalida).setOnClickListener {
            viewModel.registrar(Accion.SALIDA)
        }

        // Listener: usuario mueve el switch -> actualiza zona en VM
        swZona.setOnCheckedChangeListener { _, checked ->
            if (updatingFromVm) return@setOnCheckedChangeListener
            viewModel.setZone(if (checked) Ubicacion.DENTRO_RANGO else Ubicacion.FUERA_RANGO)
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
                                // Si quieres, aquí puedes usar tu NotificationGatewayLocal
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

                        // habilita/inhabilita botones según política
                        // findViewById<Button>(R.id.btnEntrada).isEnabled = st.puedeRegistrar
                        // findViewById<Button>(R.id.btnSalida).isEnabled  = st.puedeRegistrar
                    }
                }
            }
        }
    }

    private fun verificarSesionActiva() {
        val session = Singletons.session.getSessionInfo()
        if (!session.isLoggedIn) {
            Toast.makeText(
                this,
                "Tu sesión ha caducado. Inicia sesión nuevamente.",
                Toast.LENGTH_LONG
            ).show()

            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }


    override fun onStart() {
        super.onStart()
        verificarSesionActiva()
        // Igual que en MainActivity
        time.start(1000L)
    }

    override fun onStop() {
        viewModel.stopClock()
        super.onStop()
    }
}