package com.example.alden.session

data class SessionInfo(
    val isLoggedIn: Boolean,
    val loginType: LoginType?,   // null si no hay sesión
    val userId: String?,         // uid de Firebase o id/usuario local
    val email: String?,          // email si lo hay, sino null
    val displayName: String?,    // nombre para mostrar
    val photoUrl: String?
) {
    val isGoogleLogin: Boolean
        get() = isLoggedIn && loginType == LoginType.GOOGLE

    val isLocalLogin: Boolean
        get() = isLoggedIn && loginType == LoginType.LOCAL
}
