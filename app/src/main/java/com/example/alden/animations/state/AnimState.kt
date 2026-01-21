package com.example.alden.animations.state

data class AnimState(
    val lastAction: String?,
    val lastActionTimestamp: Long,
    val lastRegisterSuccess: Boolean
)
