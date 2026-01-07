package com.example.alden.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alden.di.Singletons
import com.example.alden.flow.attendance.AttendanceRepository
import com.example.alden.flow.auth.AuthSource
import com.example.alden.flow.location.LocationSource
import com.example.alden.flow.policy.PolicyEngine
import com.example.alden.flow.time.TimeSource
import com.example.alden.models.Accion
import com.example.alden.models.RegistroAcceso
import com.example.alden.models.Rol
import com.example.alden.models.Ubicacion
import com.example.alden.models.Usuario
import com.example.alden.notifications.NotificationChannelType
import com.example.alden.presentation.state.AppEvent
import com.example.alden.presentation.state.AppState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime

class MainViewModel(
    private val authSource: AuthSource,
    private val locationSource: LocationSource,
    private val timeSource: TimeSource,
    private val attendanceRepository: AttendanceRepository,
    private val policyEngine: PolicyEngine
) : ViewModel() {

    // canRegister como StateFlow para lectura inmediata desde UI/acciones
    private val canRegisterState: StateFlow<Boolean> =
        policyEngine.canRegister.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    // Eventos one-shot para UI / NotificationGatewayLocal
    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    // Estado agregado de la pantalla como StateFlow
    // Cumple req: "Usuario estándar intentando ver registros del Admin -> acceso denegado"
    // mediante el filtrado en la variable 'visibles'.
    val appState: StateFlow<AppState> = combine(
        authSource.userFlow,
        locationSource.zoneFlow,
        timeSource.timeFlow,
        attendanceRepository.recordsFlow,
        canRegisterState
    ) { user, zone, now, records, canReg ->

        // Lógica de visualización segura por ROL
        val visibles = when {
            user == null -> emptyList()
            user.rol == Rol.ADMIN -> records                             // Admin ve todo [cite: 18]
            else -> records.filter { it.usuario.id == user.id }          // User solo los suyos [cite: 17]
        }.sortedByDescending { it.hora }

        AppState(
            usuario = user,
            ubicacion = zone,
            horaActual = now,
            puedeRegistrar = canReg,
            registros = visibles,
            mensajeEstado = if (canReg) "HABILITADO" else "DESHABILITADO"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppState(
            usuario = authSource.userFlow.value,
            ubicacion = locationSource.zoneFlow.value,
            horaActual = timeSource.timeFlow.value,
            puedeRegistrar = canRegisterState.value,
            registros = attendanceRepository.recordsFlow.value,
            mensajeEstado = if (canRegisterState.value) "HABILITADO" else "DESHABILITADO"
        )
    )

    // --- API para la UI ---

    fun stopClock() = timeSource.stop()

    fun setZone(zone: Ubicacion) = viewModelScope.launch {
        locationSource.setZone(zone)
    }

    fun loginAsAdminOrUser(usuario: Usuario) = viewModelScope.launch {
        authSource.loginAs(usuario)
    }

    fun logout() = viewModelScope.launch { authSource.logout() }

    /**
     * Intento de registrar ENTRADA/SALIDA.
     * Emite eventos Notify/ShowToast según éxito o causa de bloqueo.
     */
    fun registrar(accion: Accion) = viewModelScope.launch {
        val user = authSource.userFlow.value
        val zone = locationSource.zoneFlow.value
        val nowTime = timeSource.timeFlow.value
        val nowDateTime = LocalDateTime.now()
        val canReg = canRegisterState.value

        // Cumple req: "Admin intentando registrar asistencia -> acceso denegado"
        if (user?.rol == Rol.ADMIN) {
            _events.tryEmit(AppEvent.ShowToast("Acceso denegado: El administrador solo puede visualizar registros."))
            _events.tryEmit(
                AppEvent.Notify(
                    title = "Alerta de Seguridad",
                    body = "Intento de registro no autorizado (Rol ADMIN)",
                    channel = NotificationChannelType.ALERTAS
                )
            )
            return@launch
        }

        if (user == null) {
            _events.tryEmit(AppEvent.ShowToast("Inicia sesión para registrar asistencia"))
            _events.tryEmit(
                AppEvent.Notify(
                    title = "Alerta",
                    body = "Intento de acceso anónimo bloqueado",
                    channel = NotificationChannelType.ALERTAS
                )
            )
            return@launch
        }

        if (canReg) {
            // Registro válido
            val registro = RegistroAcceso(
                usuario = user,
                accion = accion,
                ubicacion = zone,
                hora = nowDateTime
            )
            attendanceRepository.add(registro)
            Singletons.statsStore.addAsistencia()

            _events.tryEmit(
                AppEvent.Notify(
                    title = "Asistencia",
                    body = "${user.nombre}: ${accion.name} registrada",
                    channel = NotificationChannelType.ASISTENCIA
                )
            )
            _events.tryEmit(AppEvent.ShowToast("Registro ${accion.name} exitoso"))
        } else {
            // Determinar causa para mensaje/alerta
            val reason = computeBlockReason(userEnabled = user.enabled, zone, nowTime)
            Singletons.statsStore.addInasistencia()
            _events.tryEmit(AppEvent.ShowToast("DESHABILITADO: $reason"))
            _events.tryEmit(
                AppEvent.Notify(
                    title = "Alerta",
                    body = "$reason para ${user.nombre}",
                    channel = NotificationChannelType.ALERTAS
                )
            )
        }
    }

    // --- Helpers privados ---

    private val START: LocalTime = LocalTime.of(6, 0)
    private val END: LocalTime = LocalTime.of(20, 0)

    private fun withinAllowedHours(now: LocalTime): Boolean {
        return !now.isBefore(START) && !now.isAfter(END)
    }

    private fun computeBlockReason(userEnabled: Boolean, zone: Ubicacion, now: LocalTime): String {
        return when {
            !userEnabled -> "Usuario deshabilitado"
            !withinAllowedHours(now) -> "Fuera de horario (permitido 06:00–20:00)"
            zone == Ubicacion.FUERA_RANGO -> "Fuera de zona"
            else -> "Política no satisfecha"
        }
    }
}