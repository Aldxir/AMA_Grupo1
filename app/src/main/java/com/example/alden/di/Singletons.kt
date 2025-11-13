package com.example.alden.di

import com.example.alden.flow.auth.AuthSource
import com.example.alden.flow.auth.AuthSourceImpl
import com.example.alden.flow.attendance.AttendanceRepository
import com.example.alden.flow.attendance.AttendanceRepositoryImpl
import com.example.alden.flow.location.LocationSource
import com.example.alden.flow.location.LocationSourceImpl
import com.example.alden.flow.policy.PolicyEngine
import com.example.alden.flow.time.TimeSource
import com.example.alden.flow.time.TimeSourceImpl
import com.example.alden.models.Ubicacion

object Singletons {
    val auth: AuthSource by lazy { AuthSourceImpl() }
    val location: LocationSource by lazy { LocationSourceImpl(Ubicacion.DENTRO_RANGO) }
    val time: TimeSource by lazy { TimeSourceImpl() }
    val attendance: AttendanceRepository by lazy { AttendanceRepositoryImpl() }
    val policy: PolicyEngine by lazy { PolicyEngine(auth, location, time) }
}