package com.example.alden.ui.rating

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alden.R
import com.example.alden.data.UserRepository // Importante: Importar el repositorio de usuarios
import com.example.alden.di.Singletons

class AdminRatingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_ratings)

        val rv = findViewById<RecyclerView>(R.id.rvAdminRatings)
        rv.layoutManager = LinearLayoutManager(this)

        // Obtener datos del repositorio de ratings
        val ratings = Singletons.ratingRepo.getAllRatings()
        rv.adapter = AdminRatingAdapter(ratings)
    }
}

// Adaptador interno
class AdminRatingAdapter(private val list: List<UserRating>) : RecyclerView.Adapter<AdminRatingAdapter.VH>() {

    class VH(val tv: TextView) : RecyclerView.ViewHolder(tv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // Un diseño simple para cada item de la lista
        val tv = TextView(parent.context).apply {
            setPadding(32, 32, 32, 32)
            textSize = 16f
        }
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]


        val usuarioEncontrado = UserRepository.findById(item.userId)
        val nombreMostrar = usuarioEncontrado?.nombre ?: "ID: ${item.userId}"

        holder.tv.text = "Usuario: $nombreMostrar \nCalificación: ${item.stars} ⭐"
    }

    override fun getItemCount() = list.size
}