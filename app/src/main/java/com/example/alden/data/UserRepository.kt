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
            enabled = true,
            password = "admin123"
        ),
        Usuario(
            id = "u1",
            nombre = "Aldair",
            correo = "aldair@ejemplo.com",
            edad = 25,
            rol = Rol.USER,
            enabled = true,
            password = "aldair123"
        ),
        Usuario(
            id = "u2",
            nombre = "Dennis",
            correo = "dennis@ejemplo.com",
            edad = 30,
            rol = Rol.USER,
            enabled = false,
            password = "dennis123"
        )
    )
    fun getAdmin(): Usuario = usuarios.first { it.rol == Rol.ADMIN }
    fun getAnyEnabledUser(): Usuario = usuarios.first { it.rol == Rol.USER && it.enabled }
    fun findById(id: String) = usuarios.find { it.id == id }
    fun all(): List<Usuario> = usuarios

    /**
     * Autenticación simple:
     * - id se compara ignorando mayúsculas/minúsculas
     * - contraseña es sensible a mayúsculas/minúsculas
     * - solo devuelve Usuario si está enabled
     */
    fun autenticar(id: String, contrasena: String): Usuario? {
        val idLimpio = id.trim()
        val passLimpia = contrasena.trim()

        val usuario = usuarios.find { it.id.equals(idLimpio, ignoreCase = true) }
        return if (usuario != null && usuario.enabled && usuario.password == passLimpia) {
            usuario
        } else {
            null
        }
    }

    // Buscar por correo (case-insensitive)
    fun findByCorreo(correo: String) =
        usuarios.find { it.correo.equals(correo.trim(), ignoreCase = true) }

    // Autenticar por correo
    fun autenticarPorCorreo(correo: String, password: String): Usuario? {
        val user = findByCorreo(correo)
        return if (user != null && user.enabled && user.password == password.trim()) {
            user
        } else null
    }
}