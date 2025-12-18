package com.example.alden.ui.rating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alden.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RatingViewModel(
    private val repository: RatingRepository,
    private val sessionManager: SessionManager // <-- CAMBIO AQUÍ
) : ViewModel() {

    // Estado: Número de estrellas (0 a 5)
    private val _rating = MutableStateFlow(0)
    val rating: StateFlow<Int> = _rating.asStateFlow()

    // Estado: Mensaje dinámico
    private val _message = MutableStateFlow("Selecciona una calificación")
    val message: StateFlow<String> = _message.asStateFlow()

    // Estado: Botón habilitado
    private val _isSubmitEnabled = MutableStateFlow(false)
    val isSubmitEnabled: StateFlow<Boolean> = _isSubmitEnabled.asStateFlow()

    fun setRating(stars: Int) {
        _rating.value = stars
        _isSubmitEnabled.value = stars > 0
        _message.value = when (stars) {
            1 -> "Muy insatisfecho 😠"
            2 -> "Insatisfecho 🙁"
            3 -> "Neutral 😐"
            4 -> "Satisfecho 🙂"
            5 -> "Excelente experiencia 🤩"
            else -> "Selecciona una calificación"
        }
    }

    fun submitRating(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Obtenemos el userId desde tu SessionManager
            val session = sessionManager.getSessionInfo()
            val userId = session.userId

            if (userId != null && _rating.value > 0) {
                repository.saveRating(userId, _rating.value)
                onSuccess()
            }
        }
    }
}

// Factory para inyectar dependencias
class RatingViewModelFactory(
    private val repo: RatingRepository,
    private val sessionManager: SessionManager // <-- CAMBIO AQUÍ
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RatingViewModel(repo, sessionManager) as T
    }
}