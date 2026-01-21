package com.example.alden.di

import android.content.Context
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
import com.google.firebase.auth.FirebaseAuth
import com.example.alden.session.SessionManager
import com.example.alden.ui.rating.RatingRepository
import com.example.alden.charts.data.StatsStore
import com.example.alden.animations.state.AnimStateStore

object Singletons {
    // Contexto de aplicación para poder crear SessionManager y otros que lo requieran
    private lateinit var appContext: Context

    /**
     * Debe llamarse una sola vez desde tu clase Application.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // FirebaseAuth que ya usas para login con Google
    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // NUEVO: SessionManager centralizado
    val session: SessionManager by lazy {
        SessionManager(appContext, firebaseAuth)
    }

    val auth: AuthSource by lazy { AuthSourceImpl() }
    val location: LocationSource by lazy { LocationSourceImpl(Ubicacion.DENTRO_RANGO) }
    val time: TimeSource by lazy { TimeSourceImpl() }
    val attendance: AttendanceRepository by lazy { AttendanceRepositoryImpl() }
    val policy: PolicyEngine by lazy { PolicyEngine(auth, location, time) }
    val ratingRepo: com.example.alden.ui.rating.RatingRepository by lazy {
        com.example.alden.ui.rating.RatingRepository(appContext)
    }
    val statsStore: StatsStore by lazy {
        StatsStore(appContext)
    }

    val animStateStore: AnimStateStore by lazy {
        AnimStateStore(appContext)
    }


}