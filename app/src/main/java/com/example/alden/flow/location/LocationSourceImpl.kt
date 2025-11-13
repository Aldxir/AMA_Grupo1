package com.example.alden.flow.location

import com.example.alden.models.Ubicacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationSourceImpl(
    initial: Ubicacion,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : LocationSource {

    private val _zone = MutableStateFlow(initial)
    override val zoneFlow: StateFlow<Ubicacion> = _zone .asStateFlow()

    override suspend fun setZone(zone: Ubicacion) {
        _zone.emit(zone)
    }

    // fun setZoneAsync (zone: Ubicacion) = scope.launch { setZone(zone) }
}