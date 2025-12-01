package com.example.alden.auth

import com.example.alden.models.Rol
import com.example.alden.models.Usuario
import com.google.firebase.auth.FirebaseUser

object GoogleAuthMapper {
    fun toUsuario(firebaseUser: FirebaseUser): Usuario {
        val nombre = firebaseUser.displayName ?: firebaseUser.email ?: "Usuario Google"
        val correo = firebaseUser.email ?: ""

        return Usuario(
            id = firebaseUser.uid,
            nombre = nombre,
            correo = correo,
            rol = Rol.USER,
            enabled = true,
            edad = 0
        )
    }
}