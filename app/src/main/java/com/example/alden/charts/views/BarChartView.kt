package com.example.alden.charts.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.max
import kotlin.math.min

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var asistencias: Int = 0
    private var inasistencias: Int = 0

    // 0..1 para animación
    private var animProgress: Float = 1f
    private var animator: ValueAnimator? = null

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D0D0D0")
        strokeWidth = dp(1.2f)
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EFEFEF")
        strokeWidth = dp(1f)
    }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = sp(13f)
    }

    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = sp(11f)
    }

    private val rect = RectF()

    init {
        // Para que la sombra se vea bien en la mayoría de dispositivos
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setData(asist: Int, inasist: Int, animate: Boolean) {
        asistencias = max(0, asist)
        inasistencias = max(0, inasist)

        animator?.cancel()

        if (animate) {
            animProgress = 0f
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 700L
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

        val pad = dp(16f)
        val topPad = dp(18f)
        val bottomPad = dp(44f)

        val chartLeft = pad
        val chartRight = w - pad
        val chartTop = topPad
        val chartBottom = h - bottomPad
        val chartW = chartRight - chartLeft
        val chartH = chartBottom - chartTop

        val total = asistencias + inasistencias
        if (total == 0) {
            // Mensaje “Sin datos”
            textPaint.textSize = sp(16f)
            canvas.drawText("Sin datos", w / 2f, h / 2f + textBaselineOffset(textPaint), textPaint)
            smallTextPaint.textSize = sp(12f)
            canvas.drawText(
                "Genera registros para ver comparación",
                w / 2f,
                h / 2f + dp(22f) + textBaselineOffset(smallTextPaint),
                smallTextPaint
            )
            return
        }

        // 1) Grilla simple (3 líneas)
        val lines = 3
        for (i in 0..lines) {
            val y = chartTop + (chartH * i / lines.toFloat())
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
        }

        // 2) Eje base
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint)

        // 3) Escala
        val maxVal = max(1, max(asistencias, inasistencias)).toFloat()

        // Barras: dos columnas centradas
        val gap = chartW * 0.10f
        val barW = (chartW - gap) / 2f * 0.70f
        val groupCenterLeft = chartLeft + chartW * 0.30f
        val groupCenterRight = chartLeft + chartW * 0.70f

        val assistH = (asistencias / maxVal) * chartH * animProgress
        val missH = (inasistencias / maxVal) * chartH * animProgress

        // 4) Color dinámico (asistencia verde/amarillo/rojo según ratio)
        val pct = asistencias.toFloat() / (asistencias + inasistencias).toFloat()
        val assistBase = when {
            pct >= 0.80f -> Color.parseColor("#2E7D32")
            pct >= 0.60f -> Color.parseColor("#F9A825")
            else -> Color.parseColor("#C62828")
        }
        val missBase = Color.parseColor("#546E7A")

        // 5) Barra asistencia (con gradiente + sombra)
        drawBar(
            canvas = canvas,
            centerX = groupCenterLeft,
            bottom = chartBottom,
            height = assistH,
            width = barW,
            baseColor = assistBase,
            label = "Asistencias",
            value = asistencias
        )

        // 6) Barra inasistencia
        drawBar(
            canvas = canvas,
            centerX = groupCenterRight,
            bottom = chartBottom,
            height = missH,
            width = barW,
            baseColor = missBase,
            label = "Inasistencias",
            value = inasistencias
        )
    }

    private fun drawBar(
        canvas: Canvas,
        centerX: Float,
        bottom: Float,
        height: Float,
        width: Float,
        baseColor: Int,
        label: String,
        value: Int
    ) {
        val left = centerX - width / 2f
        val right = centerX + width / 2f
        val top = bottom - height

        // Round rect
        rect.set(left, top, right, bottom)

        // Gradiente vertical suave
        barPaint.shader = LinearGradient(
            rect.left,
            rect.top,
            rect.left,
            rect.bottom,
            lighten(baseColor),
            darken(baseColor),
            Shader.TileMode.CLAMP
        )
        barPaint.setShadowLayer(dp(6f), 0f, dp(2f), Color.argb(60, 0, 0, 0))

        val r = dp(10f)
        canvas.drawRoundRect(rect, r, r, barPaint)

        // Valor arriba
        textPaint.textSize = sp(14f)
        textPaint.color = Color.DKGRAY
        canvas.drawText(
            value.toString(),
            centerX,
            (top - dp(8f)) + textBaselineOffset(textPaint),
            textPaint
        )

        // Etiqueta abajo
        smallTextPaint.textSize = sp(11f)
        smallTextPaint.color = Color.GRAY
        canvas.drawText(
            label,
            centerX,
            (bottom + dp(22f)) + textBaselineOffset(smallTextPaint),
            smallTextPaint
        )
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
    private fun sp(v: Float): Float = v * resources.displayMetrics.scaledDensity

    private fun textBaselineOffset(paint: Paint): Float {
        val fm = paint.fontMetrics
        return -(fm.ascent + fm.descent) / 2f
    }

    private fun lighten(color: Int): Int {
        val r = min(255, (Color.red(color) * 1.12f).toInt())
        val g = min(255, (Color.green(color) * 1.12f).toInt())
        val b = min(255, (Color.blue(color) * 1.12f).toInt())
        return Color.rgb(r, g, b)
    }

    private fun darken(color: Int): Int {
        val r = max(0, (Color.red(color) * 0.88f).toInt())
        val g = max(0, (Color.green(color) * 0.88f).toInt())
        val b = max(0, (Color.blue(color) * 0.88f).toInt())
        return Color.rgb(r, g, b)
    }
}
