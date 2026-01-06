package com.example.alden.ui.rating

import android.content.Context
data class UserRating(val userId: String, val stars: Int)
class RatingRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("app_ratings", Context.MODE_PRIVATE)

    fun saveRating(userId: String, stars: Int) {
        // Guardamos: clave="rating_userID", valor=estrellas
        prefs.edit().putInt("rating_$userId", stars).apply()
    }

    fun getAllRatings(): List<UserRating> {
        val allEntries = prefs.all
        val list = mutableListOf<UserRating>()

        for ((key, value) in allEntries) {
            if (key.startsWith("rating_") && value is Int) {
                val userId = key.removePrefix("rating_")
                list.add(UserRating(userId, value))
            }
        }
        return list
    }
}