package com.example.alden.animations.effects

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

/**
 * Extensiones reutilizables de animación (Práctica 11).
 * - ViewPropertyAnimator (alpha/scale/translation)
 * - ObjectAnimator / AnimatorSet (por propiedades)
 */
object ViewAnimations {

    /** Feedback rápido al presionar un Card/Botón. */
    fun press(view: View, onEnd: (() -> Unit)? = null) {
        view.animate()
            .scaleX(0.96f)
            .scaleY(0.96f)
            .alpha(0.95f)
            .setDuration(90)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(140)
                    .setInterpolator(OvershootInterpolator(1.4f))
                    .withEndAction { onEnd?.invoke() }
                    .start()
            }
            .start()
    }

    /** Animación de éxito (pulso) para indicar registro exitoso. */
    fun successPulse(view: View) {
        val sx = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.08f, 1f)
        val sy = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.08f, 1f)
        AnimatorSet().apply {
            duration = 260
            interpolator = OvershootInterpolator(1.2f)
            playTogether(sx, sy)
            start()
        }
    }



    /** Animación de error/bloqueo (shake). */
    fun shake(view: View) {
        val dx = 10f
        ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, -dx, dx, -dx, dx, 0f).apply {
            duration = 320
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    fun errorShake(view: View) {
        // Cancela animaciones previas para que no “tiemble” raro
        view.animate().cancel()

        val shake = ObjectAnimator.ofFloat(
            view,
            "translationX",
            0f, -14f, 14f, -12f, 12f, -8f, 8f, 0f
        )
        shake.duration = 420
        shake.interpolator = AccelerateDecelerateInterpolator()
        shake.start()
    }

    /** Animación de aparición suave para componentes (entrada). */
    fun enter(view: View) {
        view.alpha = 0f
        view.translationY = 20f
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(260)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    /**
     * Secuencia para demo: botón -> check -> mensaje.
     *
     * @param actionButton  el botón que se presiona
     * @param checkIcon     icono de confirmación
     * @param messageView   texto de confirmación
     */
    fun confirmSequence(actionButton: View, checkIcon: View, messageView: View) {
        // Estado inicial
        checkIcon.alpha = 0f
        checkIcon.scaleX = 0.6f
        checkIcon.scaleY = 0.6f
        messageView.alpha = 0f
        messageView.translationY = 16f

        val btnDown = ObjectAnimator.ofFloat(actionButton, View.SCALE_X, 1f, 0.94f)
        val btnDownY = ObjectAnimator.ofFloat(actionButton, View.SCALE_Y, 1f, 0.94f)
        val btnUp = ObjectAnimator.ofFloat(actionButton, View.SCALE_X, 0.94f, 1f)
        val btnUpY = ObjectAnimator.ofFloat(actionButton, View.SCALE_Y, 0.94f, 1f)

        val checkFade = ObjectAnimator.ofFloat(checkIcon, View.ALPHA, 0f, 1f)
        val checkSx = ObjectAnimator.ofFloat(checkIcon, View.SCALE_X, 0.6f, 1.15f, 1f)
        val checkSy = ObjectAnimator.ofFloat(checkIcon, View.SCALE_Y, 0.6f, 1.15f, 1f)

        val msgFade = ObjectAnimator.ofFloat(messageView, View.ALPHA, 0f, 1f)
        val msgUp = ObjectAnimator.ofFloat(messageView, View.TRANSLATION_Y, 16f, 0f)

        // 1) feedback del botón
        val buttonSet = AnimatorSet().apply {
            duration = 140
            interpolator = DecelerateInterpolator()
            playTogether(btnDown, btnDownY)
        }

        val buttonBack = AnimatorSet().apply {
            duration = 180
            interpolator = OvershootInterpolator(1.4f)
            playTogether(btnUp, btnUpY)
        }

        // 2) confirmación simultánea (check + mensaje)
        val confirmSet = AnimatorSet().apply {
            duration = 300
            interpolator = OvershootInterpolator(1.2f)
            playTogether(checkFade, checkSx, checkSy, msgFade, msgUp)
        }

        AnimatorSet().apply {
            playSequentially(buttonSet, buttonBack, confirmSet)
            start()
        }
    }
}
