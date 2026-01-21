package com.example.alden.animations.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.alden.R
import com.example.alden.accesscontrol.ScreenGuard
import com.example.alden.animations.effects.ViewAnimations
import com.example.alden.animations.state.AnimState
import com.example.alden.animations.transitions.TransitionNavigator
import com.example.alden.animations.transitions.TransitionType
import com.example.alden.auth.AuthSession
import com.example.alden.auth.GoogleAuthMapper
import com.example.alden.data.UserRepository
import com.example.alden.di.Singletons
import com.example.alden.models.Usuario
import com.example.alden.ui.login.LoginActivity

/**
 * Pantalla de demostración de animaciones (Práctica 11).
 * - Animaciones de vistas: alpha/scale/translation/rotation (ViewPropertyAnimator)
 * - Animaciones por propiedades: ObjectAnimator / AnimatorSet
 * - Transiciones entre Activities (Fade / Slide / Scale)
 * - Estado persistente para disparar animaciones (SharedPreferences)
 */
class AnimationsActivity : AppCompatActivity() {

    private var currentUser: Usuario? = null

    private lateinit var tvState: TextView
    private lateinit var imgStateIndicator: ImageView

    private var pulseAnim: AnimatorSet? = null

    private val prefsListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        renderState(Singletons.animStateStore.read(), animate = true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animations)

        val session = Singletons.session.getSessionInfo()
        if (!session.isLoggedIn) {
            ScreenGuard.redirectToLogin(this)
            return
        }

        val state = Singletons.animStateStore.read()
        currentUser = resolveUser()

        // Protección de pantalla: sin sesión -> Login
        if (!ScreenGuard.validateSession(this, currentUser)) return

        tvState = findViewById(R.id.tvAnimState)
        imgStateIndicator = findViewById(R.id.imgStateIndicator)

        // UI: muestra el estado en pantalla
        tvState.text = "Última acción: ${state.lastAction}\nÉxito: ${state.lastRegisterSuccess}\nFecha: ${state.lastActionTimestamp}"


        val btnDemo = findViewById<Button>(R.id.btnDemoRegister)
        val imgCheck = findViewById<ImageView>(R.id.imgCheck)
        val tvMsg = findViewById<TextView>(R.id.tvMsg)

        val btnPulse = findViewById<Button>(R.id.btnStartPulse)
        val btnPulseStop = findViewById<Button>(R.id.btnStopPulse)
        val imgPulse = findViewById<ImageView>(R.id.imgPulse)

        val btnGoDetail = findViewById<Button>(R.id.btnGoDetail)
        val btnGoStats = findViewById<Button>(R.id.btnGoStats)

        val btnClear = findViewById<Button>(R.id.btnClearState)
        val cardDemo = findViewById<View>(R.id.cardDemo)

        if (state.lastRegisterSuccess) {
            ViewAnimations.successPulse(cardDemo)
            tvMsg.text = "Última acción exitosa ✅"
        } else if (!state.lastAction.isNullOrBlank()) {
            ViewAnimations.errorShake(cardDemo)
            tvMsg.text = "Última acción fallida ❌"
        } else {
            tvMsg.text = "Sin acciones registradas aún"
        }


        // Estado inicial
        renderState(Singletons.animStateStore.read(), animate = false)

        // Entrada suave de la UI
        ViewAnimations.enter(findViewById(R.id.cardDemo))
        ViewAnimations.enter(findViewById(R.id.cardPulse))
        ViewAnimations.enter(findViewById(R.id.cardTransitions))
        ViewAnimations.enter(findViewById(R.id.cardState))

        // 1) Secuencia encadenada: botón -> confirmación -> mensaje
        btnDemo.setOnClickListener {
            ViewAnimations.confirmSequence(btnDemo, imgCheck, tvMsg)
            Singletons.animStateStore.writeLastAction("REGISTRO_DEMO", success = true)
        }

        // 2) Simultánea y repetitiva: pulso/rotación del icono
        btnPulse.setOnClickListener {
            startPulse(imgPulse)
        }
        btnPulseStop.setOnClickListener {
            stopPulse(imgPulse)
        }

        // 3) Transiciones (Fade / Slide)
        btnGoDetail.setOnClickListener {
            val intent = android.content.Intent(this, DetailActivity::class.java)
            TransitionNavigator.run { launch(intent, TransitionType.SLIDE) }
        }

        btnGoStats.setOnClickListener {
            val intent = android.content.Intent(this, com.example.alden.charts.ui.ChartsActivity::class.java)
            TransitionNavigator.run { launch(intent, TransitionType.FADE) }
        }

        // 4) Estado persistente (SharedPreferences)
        btnClear.setOnClickListener {
            ViewAnimations.press(btnClear) {
                Singletons.animStateStore.clear()
                renderState(Singletons.animStateStore.read(), animate = true)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // Si se cerró sesión en otro lado, bloquea aquí
        if (!Singletons.session.getSessionInfo().isLoggedIn) {
            ScreenGuard.redirectToLogin(this)
            return
        }
        Singletons.animStateStore.addListener(prefsListener)
        renderState(Singletons.animStateStore.read(), animate = false)
    }

    override fun onPause() {
        Singletons.animStateStore.removeListener(prefsListener)
        super.onPause()
    }

    override fun finish() {
        super.finish()
        val type = TransitionNavigator.run { readTransitionType() }
        TransitionNavigator.run { applyFinishTransition(type) }
    }

    private fun renderState(state: AnimState, animate: Boolean) {
        val label = buildString {
            append("Última acción: ")
            append(state.lastAction ?: "(ninguna)")
            append("\nÉxito: ")
            append(if (state.lastRegisterSuccess) "Sí" else "No")
        }
        tvState.text = label

        // Indicador animado dependiente del estado
        imgStateIndicator.visibility = View.VISIBLE
        imgStateIndicator.alpha = 1f
        imgStateIndicator.rotation = 0f

        if (state.lastRegisterSuccess) {
            imgStateIndicator.setImageResource(android.R.drawable.checkbox_on_background)
            if (animate) ViewAnimations.successPulse(imgStateIndicator)
        } else {
            imgStateIndicator.setImageResource(android.R.drawable.ic_delete)
            if (animate && state.lastAction != null) ViewAnimations.shake(imgStateIndicator)
        }
    }

    private fun startPulse(view: View) {
        stopPulse(view)
        val rot = ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f).apply {
            duration = 900
            interpolator = LinearInterpolator()
            repeatCount = ObjectAnimator.INFINITE
        }
        val sx = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.12f, 1f).apply {
            duration = 650
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = ObjectAnimator.INFINITE
        }
        val sy = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.12f, 1f).apply {
            duration = 650
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = ObjectAnimator.INFINITE
        }
        pulseAnim = AnimatorSet().apply {
            playTogether(rot, sx, sy)
            start()
        }
        Singletons.animStateStore.writeLastAction("PULSE_ON", success = true)
    }

    private fun stopPulse(view: View) {
        pulseAnim?.cancel()
        pulseAnim = null
        view.rotation = 0f
        view.scaleX = 1f
        view.scaleY = 1f
    }

    private fun resolveUser(): Usuario? {
        val extraUserId = intent.getStringExtra(LoginActivity.EXTRA_USER_ID)
        val firebaseUser = AuthSession.currentUser
        val usuarioLocal = extraUserId?.let { UserRepository.findById(it) }
        return usuarioLocal ?: firebaseUser?.let { GoogleAuthMapper.toUsuario(it) }
    }

    override fun onStart() {
        super.onStart()
        if (!Singletons.session.getSessionInfo().isLoggedIn) {
            ScreenGuard.redirectToLogin(this)
            return
        }
    }

}
