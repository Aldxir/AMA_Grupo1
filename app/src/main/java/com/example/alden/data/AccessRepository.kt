package com.example.alden.data

import com.example.alden.models.RegistroAcceso

object AccessRepository {
    private val registros = mutableListOf<RegistroAcceso>()

    fun add(reg: RegistroAcceso) { registros += reg}

    fun all(): List<RegistroAcceso> = registros.toList()

    fun byUser(userId: String): List<RegistroAcceso> =
        registros.filter { it.usuario.id == userId }
}