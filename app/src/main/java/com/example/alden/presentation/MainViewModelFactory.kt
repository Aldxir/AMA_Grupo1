package com.example.alden.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alden.flow.attendance.AttendanceRepository
import com.example.alden.flow.auth.AuthSource
import com.example.alden.flow.location.LocationSource
import com.example.alden.flow.policy.PolicyEngine
import com.example.alden.flow.time.TimeSource

class MainViewModelFactory(
    private val auth: AuthSource,
    private val location: LocationSource,
    private val time: TimeSource,
    private val attendance: AttendanceRepository,
    private val policy: PolicyEngine
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(auth, location, time, attendance, policy) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}