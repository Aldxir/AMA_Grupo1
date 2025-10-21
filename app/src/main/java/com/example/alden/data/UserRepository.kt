package com.example.alden.data

import com.example.alden.models.Rol
import com.example.alden.models.Usuario

object UserRepository {
    private val usuarios = listOf(
        Usuario(
            id = "admin",
            nombre = "Admin",
            correo = "admin@ejemplo.com",
            edad = 20,
            rol = Rol.ADMIN,
            enabled = true
        ),
        Usuario(
            id = "u1",
            nombre = "Ana",
            correo = "ana@ejemplo.com",
            edad = 25,
            rol = Rol.USER,
            enabled = true
        ),
        Usuario(
            id = "u2",
            nombre = "Juan",
            correo = "juan@ejemplo.com",
            edad = 30,
            rol = Rol.USER,
            enabled = false
        )
    )
    fun getAdmin(): Usuario = usuarios.first { it.rol == Rol.ADMIN }
    fun getAnyEnabledUser(): Usuario = usuarios.first { it.rol == Rol.USER && it.enabled }
    fun findById(id: String) = usuarios.find { it.id == id }
    fun all(): List<Usuario> = usuarios
}