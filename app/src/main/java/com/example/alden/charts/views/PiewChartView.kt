package com.example.alden.charts.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.max
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var asistencias: Int = 0
    private var inasistencias: Int = 0

    // Animación: 0..1
    private var animProgress: Float = 1f
    private var animator: ValueAnimator? = null

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val legendPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val arcRect = RectF()

    init {
        // Shadow suave (requiere software layer para verse consistente)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    /**
     * Alimenta el gráfico.
     * @param animate si true, se anima el render (simple) al dibujar
     */
    fun setData(asist: Int, inasist: Int, animate: Boolean) {
        asistencias = max(0, asist)
        inasistencias = max(0, inasist)

        animator?.cancel()

        if (animate) {
            animProgress = 0f
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 750L
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    animProgress = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            animProgress = 1f
            invalidate()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val total = asistencias + inasistencias
        val pad = dp(16f)
        val cx = w / 2f
        val cy = h / 2f

        // Radio y grosor del anillo
        val radius = min(w, h) * 0.32f
        val stroke = dp(18f)
        ringPaint.strokeWidth = stroke
        slicePaint.strokeWidth = stroke

        // Área del arco
        arcRect.set(
            cx - radius,
            cy - radius,
            cx + radius,
            cy + radius
        )

        // 1) Si no hay datos -> mensaje
        if (total == 0) {
            // Ring base
            ringPaint.color = Color.parseColor("#E0E0E0")
            ringPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
            canvas.drawArc(arcRect, 0f, 360f, false, ringPaint)

            textPaint.textSize = sp(18f)
            canvas.drawText("Sin datos", cx, cy + textBaselineOffset(textPaint), textPaint)

            smallTextPaint.textSize = sp(12f)
            canvas.drawText(
                "Registra asistencias para ver estadísticas",
                cx,
                cy + dp(22f) + textBaselineOffset(smallTextPaint),
                smallTextPaint
            )
            return
        }

        // 2) Base ring (fondo)
        ringPaint.color = Color.parseColor("#EEEEEE")
        ringPaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
        canvas.drawArc(arcRect, 0f, 360f, false, ringPaint)

        // 3) Calcular porcentaje y colores dinámicos
        val pct = asistencias.toFloat() / total.toFloat() // 0..1
        val pctInt = (pct * 100f).toInt()

        val assistBase = when {
            pct >= 0.80f -> Color.parseColor("#2E7D32") // verde
            pct >= 0.60f -> Color.parseColor("#F9A825") // amarillo
            else -> Color.parseColor("#C62828")         // rojo
        }
        val missBase = Color.parseColor("#546E7A") // gris-azulado

        // 4) Arcos (animados)
        val startAngle = -90f
        val sweepAssist = 360f * pct
        val sweepMiss = 360f - sweepAssist

        // Gradiente “sweep” para asistencia (efecto visual)
        val assistGradient = SweepGradient(
            cx, cy,
            intArrayOf(lighten(assistBase), assistBase, darken(assistBase)),
            floatArrayOf(0f, 0.6f, 1f)
        )

        // Slice asistencia
        slicePaint.shader = assistGradient
        slicePaint.setShadowLayer(dp(6f), 0f, dp(2f), Color.argb(70, 0, 0, 0))
        canvas.drawArc(
            arcRect,
            startAngle,
            sweepAssist * animProgress,
            false,
            slicePaint
        )

        // Slice inasistencia (sin gradiente, pero con sombra)
        slicePaint.shader = null
        slicePaint.color = missBase
        slicePaint.setShadowLayer(dp(4f), 0f, dp(2f), Color.argb(55, 0, 0, 0))
        canvas.drawArc(
            arcRect,
            startAngle + sweepAssist,
            sweepMiss * animProgress,
            false,
            slicePaint
        )

        // 5) Texto central
        textPaint.textSize = sp(26f)
        textPaint.color = Color.DKGRAY
        canvas.drawText(
            "$pctInt%",
            cx,
            cy + textBaselineOffset(textPaint),
            textPaint
        )

        smallTextPaint.textSize = sp(12f)
        smallTextPaint.color = Color.GRAY
        canvas.drawText(
            "Asistencia",
            cx,
            cy + dp(22f) + textBaselineOffset(smallTextPaint),
            smallTextPaint
        )

        // 6) Leyenda simple abajo (dos bullets + valores)
        val legendY = h - pad
        val leftX = cx - dp(90f)
        val rightX = cx + dp(90f)

        legendPaint.color = assistBase
        canvas.drawCircle(leftX, legendY - dp(6f), dp(6f), legendPaint)
        smallTextPaint.textSize = sp(12f)
        smallTextPaint.color = Color.DKGRAY
        canvas.drawText("OK: $asistencias", leftX + dp(52f), legendY, smallTextPaint)

        legendPaint.color = missBase
        canvas.drawCircle(rightX, legendY - dp(6f), dp(6f), legendPaint)
        canvas.drawText("NO: $inasistencias", rightX + dp(52f), legendY, smallTextPaint)
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
    private fun sp(v: Float): Float = v * resources.displayMetrics.scaledDensity

    private fun textBaselineOffset(paint: Paint): Float {
        val fm = paint.fontMetrics
        return -(fm.ascent + fm.descent) / 2f
    }

    private fun lighten(color: Int): Int {
        val r = min(255, (Color.red(color) * 1.15f).toInt())
        val g = min(255, (Color.green(color) * 1.15f).toInt())
        val b = min(255, (Color.blue(color) * 1.15f).toInt())
        return Color.rgb(r, g, b)
    }

    private fun darken(color: Int): Int {
        val r = max(0, (Color.red(color) * 0.85f).toInt())
        val g = max(0, (Color.green(color) * 0.85f).toInt())
        val b = max(0, (Color.blue(color) * 0.85f).toInt())
        return Color.rgb(r, g, b)
    }
}
