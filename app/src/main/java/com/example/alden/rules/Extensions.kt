package com.example.alden.rules

import com.example.alden.models.Rol
import com.example.alden.models.Usuario
import java.time.LocalTime

fun Usuario.displayName(): String = nombre
fun Usuario.isEnabled(): Boolean = enabled
fun Usuario.isAdmin(): Boolean = rol == Rol.ADMIN

fun LocalTime.allowedAt(): Boolean {
    val start = LocalTime.of(6, 0)
    val end = LocalTime.of(20, 0)

    return !isBefore(start) && !isAfter(end)
}

// fun LocalDateTime.allowedAt(): Boolean = toLocalTime().allowedAt()