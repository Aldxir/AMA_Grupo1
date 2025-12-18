package com.example.alden.ui.rating

import android.content.Context

class RatingRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("app_ratings", Context.MODE_PRIVATE)

    fun saveRating(userId: String, stars: Int) {
        // Guardamos: clave="rating_userID", valor=estrellas
        prefs.edit().putInt("rating_$userId", stars).apply()
    }

    fun getRating(userId: String): Int {
        return prefs.getInt("rating_$userId", 0) // 0 significa sin calificar
    }
}