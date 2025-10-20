package com.example.alden.models

data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val edad: Int,
    val rol: Rol,
    val enabled: Boolean
)
