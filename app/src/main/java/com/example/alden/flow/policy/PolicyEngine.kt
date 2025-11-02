package com.example.alden.flow.policy

import com.example.alden.flow.auth.AuthSource
import com.example.alden.flow.location.LocationSource
import com.example.alden.flow.time.TimeSource
import com.example.alden.models.Ubicacion
import com.example.alden.models.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalTime

class PolicyEngine(
    private val authSource: AuthSource,
    private val locationSource: LocationSource,
    private val timeSource: TimeSource
) {
    val canRegister: Flow<Boolean> = combine(
        authSource.userFlow,
        locationSource.zoneFlow,
        timeSource.timeFlow
    ) { user: Usuario?, zone: Ubicacion, now: LocalTime ->
        val enabled = user?.enabled == true
        val inHours = withinAllowedHours(now)
        val inZone = (zone == Ubicacion.DENTRO_RANGO)
        enabled && inHours && inZone
    }.distinctUntilChanged()

    private val START: LocalTime = LocalTime.of(6, 0)
    private val END: LocalTime = LocalTime.of(20, 0)

    /** 06:00 <= now <= 20:00 */
    private fun withinAllowedHours(now: LocalTime): Boolean {
        return !now.isBefore(START) && !now.isAfter(END)
    }
}