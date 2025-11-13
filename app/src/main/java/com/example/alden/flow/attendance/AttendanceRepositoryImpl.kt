package com.example.alden.flow.attendance

import com.example.alden.models.RegistroAcceso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AttendanceRepositoryImpl(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : AttendanceRepository {

    private val _records = MutableStateFlow<List<RegistroAcceso>>(emptyList())
    override val recordsFlow: StateFlow<List<RegistroAcceso>> = _records.asStateFlow()

    override suspend fun add(registro: RegistroAcceso) {
        _records.emit(_records.value + registro)
    }

    override suspend fun clear() {
        _records.emit(emptyList())
    }

    //fun addAsync(registro: RegistroAcceso) = scope.launch { add(registro) }
    //fun clearAsync() = scope.launch { clear() }
}