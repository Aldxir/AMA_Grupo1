package com.example.alden.animations.transitions

import android.app.Activity
import android.content.Intent
import com.example.alden.R

/**
 * Helper centralizado para aplicar transiciones entre Activities.
 *
 * Nota: Para Android 14+ y Predictive Back, se puede migrar a overrideActivityTransition
 * si tu profe lo pide. Para el alcance de esta práctica, overridePendingTransition es suficiente.
 */
object TransitionNavigator {

    const val EXTRA_TRANSITION_TYPE = "extra_transition_type"

    fun Activity.launch(intent: Intent, type: TransitionType) {
        intent.putExtra(EXTRA_TRANSITION_TYPE, type.name)
        startActivity(intent)
        overridePendingTransition(enterAnim(type), exitAnim(type))
    }

    fun Activity.readTransitionType(): TransitionType {
        val name = intent.getStringExtra(EXTRA_TRANSITION_TYPE) ?: return TransitionType.FADE
        return runCatching { TransitionType.valueOf(name) }.getOrDefault(TransitionType.FADE)
    }

    /**
     * Llamar al salir (finish) para que la animación sea consistente.
     */
    fun Activity.finishWith(type: TransitionType) {
        finish()
        overridePendingTransition(popEnterAnim(type), popExitAnim(type))
    }

    /**
     * Úsalo dentro de un override fun finish() { super.finish(); ... } para evitar recursión.
     */
    fun Activity.applyFinishTransition(type: TransitionType) {
        overridePendingTransition(popEnterAnim(type), popExitAnim(type))
    }

    private fun enterAnim(type: TransitionType): Int = when (type) {
        TransitionType.FADE -> R.anim.fade_in
        TransitionType.SLIDE -> R.anim.slide_in_right
        TransitionType.SCALE -> R.anim.scale_in
    }

    private fun exitAnim(type: TransitionType): Int = when (type) {
        TransitionType.FADE -> R.anim.fade_out
        TransitionType.SLIDE -> R.anim.slide_out_left
        TransitionType.SCALE -> R.anim.scale_out
    }

    private fun popEnterAnim(type: TransitionType): Int = when (type) {
        TransitionType.FADE -> R.anim.fade_in
        TransitionType.SLIDE -> R.anim.slide_in_left
        TransitionType.SCALE -> R.anim.scale_in
    }

    private fun popExitAnim(type: TransitionType): Int = when (type) {
        TransitionType.FADE -> R.anim.fade_out
        TransitionType.SLIDE -> R.anim.slide_out_right
        TransitionType.SCALE -> R.anim.scale_out
    }
}
