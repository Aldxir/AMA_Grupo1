package com.example.alden.ui.rating

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.alden.R
import com.example.alden.accesscontrol.ScreenGuard
import com.example.alden.auth.AuthSession
import com.example.alden.di.Singletons
import kotlinx.coroutines.launch

class RatingActivity : AppCompatActivity() {

    private val viewModel: RatingViewModel by viewModels {
        RatingViewModelFactory(Singletons.ratingRepo, Singletons.session)
    }
    private lateinit var stars: List<ImageView>
    private lateinit var tvMessage: TextView
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        // 1. Protección de Pantalla (Requisito Práctica)
        // Validamos si hay usuario (firebase o local)
        val user = AuthSession.currentUser
        // Nota: para usuarios locales sin Firebase, deberías obtenerlo del SessionManager o ViewModel compartido,
        // pero para la validación rápida, ScreenGuard con SessionManager es ideal:
        val session = Singletons.session.getSessionInfo()
        if (!session.isLoggedIn) {
            ScreenGuard.redirectToLogin(this)
            return
        }

        // 2. Referencias UI
        stars = listOf(
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5)
        )
        tvMessage = findViewById(R.id.tvRatingMessage)
        btnSubmit = findViewById(R.id.btnEnviarRating)

        // 3. Listeners de clicks en estrellas
        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                viewModel.setRating(index + 1)
            }
        }

        btnSubmit.setOnClickListener {
            viewModel.submitRating {
                Toast.makeText(this, "¡Gracias por tu opinión!", Toast.LENGTH_SHORT).show()
                finish() // Regresa al Dashboard
            }
        }

        // 4. Observar el ViewModel
        lifecycleScope.launch {
            viewModel.rating.collect { currentRating ->
                updateStarsUI(currentRating)
            }
        }

        lifecycleScope.launch {
            viewModel.message.collect { msg ->
                tvMessage.text = msg
            }
        }

        lifecycleScope.launch {
            viewModel.isSubmitEnabled.collect { isEnabled ->
                btnSubmit.isEnabled = isEnabled
                btnSubmit.alpha = if (isEnabled) 1.0f else 0.5f
            }
        }
    }

    private fun updateStarsUI(rating: Int) {
        // Lógica visual: Si rating es 3, llena estrellas indices 0, 1, 2. Vacía 3, 4.
        stars.forEachIndexed { index, imageView ->
            if (index < rating) {
                imageView.setImageResource(R.drawable.ic_star_filled)
            } else {
                imageView.setImageResource(R.drawable.ic_star_border)
            }
        }
    }
}