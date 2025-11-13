package com.example.alden

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alden.data.AccessRepository
import com.example.alden.data.UserRepository
import com.example.alden.di.Singletons
import com.example.alden.flow.attendance.AttendanceRepositoryImpl
import com.example.alden.flow.auth.AuthSourceImpl
import com.example.alden.flow.location.LocationSourceImpl
import com.example.alden.flow.policy.PolicyEngine
import com.example.alden.flow.time.TimeSource
import com.example.alden.flow.time.TimeSourceImpl
import com.example.alden.models.Accion
import com.example.alden.models.RegistroAcceso
import com.example.alden.models.Rol
import com.example.alden.models.Ubicacion
import com.example.alden.models.Usuario
import com.example.alden.notifications.NotificationChannels
import com.example.alden.notifications.NotificationGatewayLocal
import com.example.alden.presentation.MainViewModel
import com.example.alden.presentation.MainViewModelFactory
import com.example.alden.presentation.state.AppEvent
import com.example.alden.rules.displayName
import com.example.alden.services.AttendanceService
import com.example.alden.ui.RegistroAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var notifier: NotificationGatewayLocal
    private val requestPostNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted -> opcionalmente muestra un Toast si lo niegan */ }

    private lateinit var adapter: RegistroAdapter
    private val time: TimeSource = Singletons.time

    // Usuarios demo
    private val adminDemo = Usuario(
        id = "admin", nombre = "Admin",
        correo = "admin@epn.edu.ec", edad = 30,
        rol = Rol.ADMIN, enabled = true
    )
    private val userDemo = Usuario(
        id = "u1", nombre = "Juan Pérez",
        correo = "juan@epn.edu.ec", edad = 22,
        rol = Rol.USER, enabled = true
    )

    private val user2 = Usuario(
        id = "u2", nombre = "María López",
        correo = "maria@epn.edu.ec", edad = 21,
        rol = Rol.USER, enabled = true
    )

    private val demoUsers = listOf(userDemo, user2)

    private var nextUserIdx = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias UI
        val tvUsuario = findViewById<TextView>(R.id.tvUsuario)
        val tvEstado  = findViewById<TextView>(R.id.tvEstado)
        val btnLoginUser  = findViewById<Button>(R.id.btnLoginUser)
        val btnLoginAdmin = findViewById<Button>(R.id.btnLoginAdmin)
        val btnLogout     = findViewById<Button>(R.id.btnLogout)
        val btnDentro = findViewById<Button>(R.id.btnDentro)
        val btnFuera  = findViewById<Button>(R.id.btnFuera)
        val btnEntrada = findViewById<Button>(R.id.btnEntrada)
        val btnSalida  = findViewById<Button>(R.id.btnSalida)
        val rv = findViewById<RecyclerView>(R.id.rvRegistros)

        // RecyclerView
        adapter = RegistroAdapter()
        rv.adapter = adapter
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(this)

        // Clicks
        btnLoginUser.setOnClickListener  {
            val u = demoUsers[nextUserIdx]
            nextUserIdx = (nextUserIdx + 1) % demoUsers.size
            viewModel.loginAsAdminOrUser(u)
            Toast.makeText(this, "Login: ${u.nombre}", Toast.LENGTH_SHORT).show()
        }
        btnLoginAdmin.setOnClickListener { viewModel.loginAsAdminOrUser(adminDemo) }
        btnLogout.setOnClickListener     { viewModel.logout() }

        btnDentro.setOnClickListener { viewModel.setZone(Ubicacion.DENTRO_RANGO) }
        btnFuera.setOnClickListener  { viewModel.setZone(Ubicacion.FUERA_RANGO) }

        btnEntrada.setOnClickListener { viewModel.registrar(Accion.ENTRADA) }
        btnSalida.setOnClickListener  { viewModel.registrar(Accion.SALIDA) }


        // 1) Crear dependencias
        val factory = MainViewModelFactory(
            Singletons.auth,
            Singletons.location,
            Singletons.time,
            Singletons.attendance,
            Singletons.policy
        )
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        // 3) Notificaciones: crear canales y preparar gateway
        NotificationChannels.ensureCreated(this)
        notifier = NotificationGatewayLocal(this)
        if (Build.VERSION.SDK_INT >= 33) {
            requestPostNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 3) Coleccionar estado y eventos (UI real: actualiza vistas/botones/lista)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.appState.collect { state ->
                        // Usuario (nombre y rol) o “(sin usuario)”
                        tvUsuario.text = state.usuario?.let { "${it.nombre} (${it.rol})" } ?: "(sin usuario)"

                        // Texto de estado
                        tvEstado.text = state.mensajeEstado

                        // Habilitar si hay USER habilitado (independiente de la política)
                        val esUser          = (state.usuario?.rol == Rol.USER)
                        val userHabilitado  = (state.usuario?.enabled == true)

                        btnEntrada.isEnabled = esUser && userHabilitado
                        btnSalida.isEnabled  = esUser && userHabilitado

                        // Lista de registros
                        adapter.submit(state.registros)

                        Log.d("POLICY", "hora=${state.horaActual} puede=${state.puedeRegistrar} zone=${state.ubicacion} user=${state.usuario?.rol} enabled=${state.usuario?.enabled}")
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is AppEvent.ShowToast ->
                                Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()

                            is AppEvent.Notify -> {
                                if (hasPostNotifications()) {
                                    notifier.handle(event)
                                } else if (Build.VERSION.SDK_INT >= 33) {
                                    requestPostNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    // fallback visible por si el usuario niega el permiso
                                    Toast.makeText(this@MainActivity, "${event.title}: ${event.body}", Toast.LENGTH_SHORT).show()
                                } else {
                                    notifier.handle(event)                 // < API 33 no requiere permiso
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private fun hasPostNotifications(): Boolean =
        if (Build.VERSION.SDK_INT >= 33)
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        else true


    override fun onStart() {
        super.onStart()
        time.start(1000L) // emite la hora real cada segundo
    }

    override fun onStop() {
        // Detén el ticker para ahorrar batería
        viewModel.stopClock()
        super.onStop()
        time.stop()
    }
}
