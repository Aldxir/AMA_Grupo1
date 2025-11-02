package com.example.alden.flow.time

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalTime

class TimeSourceImpl(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : TimeSource {
    private val _time = MutableStateFlow(LocalTime.now())
    override val timeFlow: StateFlow<LocalTime> = _time.asStateFlow()

    private var job: Job? = null

    override fun start(tickMillis: Long) {
        if (job?.isActive == true) return
        job = scope.launch {
            while(isActive) {
                _time.emit(LocalTime.now())
                delay(tickMillis)
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }

}