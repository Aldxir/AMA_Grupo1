package com.example.alden.ui.registro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alden.R
import com.example.alden.accesscontrol.ScreenGuard
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
import com.example.alden.ui.rating.AdminRatingsActivity
import com.example.alden.ui.rating.RatingActivity
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    private val time: TimeSource = Singletons.time
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            Singletons.auth, Singletons.location, Singletons.time, Singletons.attendance, Singletons.policy
        )
    }
    private lateinit var adapter: RegistroAdapter
    private var updatingFromVm = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // --- 1. SETUP DE UI ---
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        val imgUserPhoto = findViewById<ImageView>(R.id.imgUserPhoto)

        // Controles de Zona (dentro de la Card de Historial)
        val tvEstado = findViewById<TextView>(R.id.tvEstado)
        val swZona = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.swZona)
        val layoutUserControls = findViewById<LinearLayout>(R.id.layoutUserControls) // NUEVO: Contenedor

        // Tarjetas
        val cardEntrada = findViewById<CardView>(R.id.cardEntrada)
        val cardSalida = findViewById<CardView>(R.id.cardSalida)
        val cardRate = findViewById<CardView>(R.id.cardRate)
        val cardAdminRatings = findViewById<CardView>(R.id.cardAdminRatings)
        val cardLogout = findViewById<CardView>(R.id.cardLogout)

        // --- 2. RECUPERAR USUARIO ---
        val extraUserId = intent.getStringExtra(LoginActivity.EXTRA_USER_ID)
        val firebaseUser = AuthSession.currentUser

        if (extraUserId == null && firebaseUser == null) {
            ScreenGuard.redirectToLogin(this)
            return
        }

        var usuarioFinal: Usuario? = null
        val usuarioLocal = extraUserId?.let { UserRepository.findById(it) }

        if (usuarioLocal != null) {
            usuarioFinal = usuarioLocal
        } else if (firebaseUser != null) {
            usuarioFinal = GoogleAuthMapper.toUsuario(firebaseUser)
        }

        // --- 3. SEGURIDAD ---
        if (!ScreenGuard.validateSession(this, usuarioFinal)) return
        ScreenGuard.applyVisualProtection(this, usuarioFinal)

        // --- 4. LOGICA VISUAL SEGUN ROL ---

        tvUserName.text = usuarioFinal?.nombre ?: "Usuario"
        tvHeader.text = "Rol: ${usuarioFinal?.rol?.name}"

        val photoUrl = firebaseUser?.photoUrl
        if (photoUrl != null) {
            Glide.with(this).load(photoUrl).circleCrop().into(imgUserPhoto)
        }

        if (usuarioFinal?.rol == Rol.ADMIN) {
            // == VISTA DE ADMIN ==
            // Ocultar botones operativos de usuario
            cardEntrada.visibility = View.GONE
            cardSalida.visibility = View.GONE
            cardRate.visibility = View.GONE

            // Mostrar herramientas de Admin
            cardAdminRatings.visibility = View.VISIBLE

            // Ocultar controles de simulación de zona (REQUISITO CUMPLIDO)
            layoutUserControls.visibility = View.GONE

        } else {
            // == VISTA DE USUARIO ==
            cardEntrada.visibility = View.VISIBLE
            cardSalida.visibility = View.VISIBLE
            cardRate.visibility = View.VISIBLE
            cardAdminRatings.visibility = View.GONE

            // Mostrar controles de simulación
            layoutUserControls.visibility = View.VISIBLE
        }

        usuarioFinal?.let { viewModel.loginAsAdminOrUser(it) }

        // --- 5. LISTENERS ---
        cardEntrada.setOnClickListener { viewModel.registrar(Accion.ENTRADA) }
        cardSalida.setOnClickListener { viewModel.registrar(Accion.SALIDA) }

        cardRate.setOnClickListener {
            startActivity(Intent(this, RatingActivity::class.java))
        }

        cardAdminRatings.setOnClickListener {
            startActivity(Intent(this, AdminRatingsActivity::class.java))
        }

        cardLogout.setOnClickListener {
            Singletons.session.logout()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        swZona.setOnCheckedChangeListener { _, checked ->
            if (!updatingFromVm) viewModel.setZone(if (checked) Ubicacion.DENTRO_RANGO else Ubicacion.FUERA_RANGO)
        }

        // --- 6. LISTA ---
        adapter = RegistroAdapter()
        findViewById<RecyclerView>(R.id.rvRegistros).apply {
            layoutManager = LinearLayoutManager(this@RegistroActivity)
            adapter = this@RegistroActivity.adapter
        }

        // --- 7. OBSERVERS ---
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.appState.collect { st ->
                        tvEstado.text = st.mensajeEstado
                        adapter.submit(st.registros)
                    }
                }
                launch {
                    viewModel.events.collect { ev ->
                        if (ev is AppEvent.ShowToast) Toast.makeText(this@RegistroActivity, ev.message, Toast.LENGTH_SHORT).show()
                    }
                }
                launch {
                    viewModel.appState.collect { st ->
                        updatingFromVm = true
                        swZona.isChecked = (st.ubicacion == Ubicacion.DENTRO_RANGO)
                        swZona.text = if (st.ubicacion == Ubicacion.DENTRO_RANGO) "Dentro de rango" else "Fuera de rango"
                        updatingFromVm = false
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!Singletons.session.getSessionInfo().isLoggedIn) ScreenGuard.redirectToLogin(this)
        time.start(1000L)
    }

    override fun onStop() {
        viewModel.stopClock()
        super.onStop()
    }
}