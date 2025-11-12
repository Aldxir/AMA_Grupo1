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
import com.example.alden.models.RegistroAcceso
import com.example.alden.models.Rol
import com.example.alden.models.Ubicacion
import java.time.LocalDateTime
import com.example.alden.presentation.MainViewModel
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.alden.flow.attendance.AttendanceRepositoryImpl
import com.example.alden.flow.auth.AuthSourceImpl
import com.example.alden.flow.location.LocationSourceImpl
import com.example.alden.flow.policy.PolicyEngine
import com.example.alden.flow.time.TimeSource
import com.example.alden.flow.time.TimeSourceImpl
import com.example.alden.presentation.MainViewModelFactory
import com.example.alden.presentation.state.AppEvent
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    private lateinit var time: TimeSource

    // Creamos las mismas dependencias que en MainActivity y construimos el ViewModel
    private val viewModel: MainViewModel by viewModels {
        val auth = AuthSourceImpl()
        val location = LocationSourceImpl(Ubicacion.DENTRO_RANGO)
        time = TimeSourceImpl()
        val attendance = AttendanceRepositoryImpl()
        val policy = PolicyEngine(auth, location, time)
        MainViewModelFactory(auth, location, time, attendance, policy)
    }

    private lateinit var adapter: RegistroAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // 1) Recuperar usuario del Intent y loguearlo en el ViewModel
        val userId = intent.getStringExtra(LoginActivity.EXTRA_USER_ID)
        val usuario = userId?.let { UserRepository.findById(it) }
        if (usuario == null) {
            Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show()
            finish(); return
        }
        viewModel.loginAsAdminOrUser(usuario)

        // 2) UI
        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        val tvEstado = findViewById<TextView>(R.id.tvEstado)
        tvHeader.text = "${usuario.nombre} (${usuario.rol.name})"

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
        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            viewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
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
                                // Si quieres, aquí puedes usar tu NotificationGatewayLocal
                                Toast.makeText(this@RegistroActivity, "${ev.title}: ${ev.body}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Igual que en MainActivity
        time.start(1000L)
    }

    override fun onStop() {
        viewModel.stopClock()
        super.onStop()
    }
}