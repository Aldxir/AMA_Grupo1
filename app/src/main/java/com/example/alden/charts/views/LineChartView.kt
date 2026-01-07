package com.example.alden.charts.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.max
import kotlin.math.min

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var series: List<Int> = emptyList()

    // 0..1 animación
    private var animProgress: Float = 1f
    private var animator: ValueAnimator? = null

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EFEFEF")
        strokeWidth = dp(1f)
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D0D0D0")
        strokeWidth = dp(1.2f)
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(3f)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.parseColor("#3949AB") // azul índigo
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#3949AB")
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = sp(12f)
    }

    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = sp(11f)
    }

    private val path = Path()
    private val fillPath = Path()

    init {
        // sombra del trazo más suave/consistente
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    /**
     * Serie de datos (ej: 7 días o 4 semanas)
     * Se dibuja en orden, de izquierda (más antiguo) a derecha (más reciente).
     */
    fun setSeries(values: List<Int>, animate: Boolean) {
        series = values.map { max(0, it) }

        animator?.cancel()
        if (animate) {
            animProgress = 0f
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 800L
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
        val bottomPad = dp(46f)
        val leftPad = dp(34f) // para etiqueta Y

        val chartLeft = pad + leftPad
        val chartRight = w - pad
        val chartTop = topPad
        val chartBottom = h - bottomPad

        val chartW = chartRight - chartLeft
        val chartH = chartBottom - chartTop

        if (series.isEmpty()) {
            textPaint.textSize = sp(16f)
            canvas.drawText("Sin datos", w / 2f, h / 2f + textBaselineOffset(textPaint), textPaint)
            smallTextPaint.textSize = sp(12f)
            canvas.drawText(
                "Cambia a Semana/Mes o genera registros",
                w / 2f,
                h / 2f + dp(22f) + textBaselineOffset(smallTextPaint),
                smallTextPaint
            )
            return
        }

        val n = series.size
        val maxVal = max(1, series.maxOrNull() ?: 1)

        // 1) Grilla (4 bandas)
        val bands = 4
        for (i in 0..bands) {
            val y = chartTop + (chartH * i / bands.toFloat())
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
        }

        // 2) Ejes
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint) // eje X
        canvas.drawLine(chartLeft, chartTop, chartLeft, chartBottom, axisPaint)     // eje Y

        // 3) Etiquetas Y (máximo y medio)
        smallTextPaint.textAlign = Paint.Align.RIGHT
        val yMaxText = maxVal.toString()
        val yMidText = (maxVal / 2).toString()
        canvas.drawText(
            yMaxText,
            chartLeft - dp(8f),
            chartTop + textBaselineOffset(smallTextPaint),
            smallTextPaint
        )
        canvas.drawText(
            yMidText,
            chartLeft - dp(8f),
            chartTop + chartH / 2f + textBaselineOffset(smallTextPaint),
            smallTextPaint
        )
        canvas.drawText(
            "0",
            chartLeft - dp(8f),
            chartBottom + textBaselineOffset(smallTextPaint),
            smallTextPaint
        )
        smallTextPaint.textAlign = Paint.Align.CENTER

        // 4) Construir puntos (x,y) con escala
        // Espaciado X uniforme
        val dx = if (n <= 1) 0f else chartW / (n - 1).toFloat()

        fun mapY(v: Int): Float {
            val ratio = v.toFloat() / maxVal.toFloat()
            return chartBottom - (ratio * chartH)
        }

        // Animación: dibuja solo hasta cierto índice fraccionario
        val t = animProgress.coerceIn(0f, 1f)
        val maxIndexF = (n - 1) * t
        val fullIndex = maxIndexF.toInt()
        val frac = maxIndexF - fullIndex

        path.reset()
        fillPath.reset()

        // 5) Crear path principal + path de relleno
        // Punto inicial
        val x0 = chartLeft
        val y0 = mapY(series[0])
        path.moveTo(x0, y0)
        fillPath.moveTo(x0, chartBottom)
        fillPath.lineTo(x0, y0)

        // Recorrer puntos hasta fullIndex
        for (i in 1..fullIndex.coerceAtMost(n - 1)) {
            val x = chartLeft + dx * i
            val y = mapY(series[i])
            path.lineTo(x, y)
            fillPath.lineTo(x, y)
        }

        // Segmento parcial al siguiente punto (si aplica)
        val nextIndex = fullIndex + 1
        if (nextIndex <= n - 1) {
            val xPrev = chartLeft + dx * fullIndex
            val yPrev = mapY(series[fullIndex])
            val xNext = chartLeft + dx * nextIndex
            val yNext = mapY(series[nextIndex])

            val xPart = xPrev + (xNext - xPrev) * frac
            val yPart = yPrev + (yNext - yPrev) * frac

            path.lineTo(xPart, yPart)
            fillPath.lineTo(xPart, yPart)
        }

        // Cerrar relleno
        val lastX = if (nextIndex <= n - 1) {
            // si quedó parcial, el último punto es el parcial
            (chartLeft + dx * fullIndex) + (dx * frac)
        } else {
            chartLeft + dx * (n - 1)
        }
        fillPath.lineTo(lastX, chartBottom)
        fillPath.close()

        // 6) Relleno con gradiente
        fillPaint.shader = LinearGradient(
            0f, chartTop, 0f, chartBottom,
            Color.argb(90, 57, 73, 171),  // similar al linePaint pero translúcido
            Color.argb(0, 57, 73, 171),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(fillPath, fillPaint)

        // 7) Dibujo de línea + sombra suave
        linePaint.setShadowLayer(dp(4f), 0f, dp(2f), Color.argb(55, 0, 0, 0))
        canvas.drawPath(path, linePaint)

        // 8) Puntos + etiquetas X simples
        // Etiquetas: si es 7 días -> D1..D7, si es 4 -> S1..S4 (no inferimos; usamos tamaño)
        val prefix = if (n <= 4) "S" else "D"

        for (i in 0 until n) {
            val x = chartLeft + dx * i
            val y = mapY(series[i])

            // Solo dibuja puntos "ya alcanzados" por la animación
            if (i < fullIndex || (i == fullIndex && frac >= 0.98f) || t >= 0.999f) {
                canvas.drawCircle(x, y, dp(4.5f), pointPaint)
            }

            // Etiquetas X: para no saturar, muestra todas si n<=7; si n>7 muestra cada 2
            val show = (n <= 7) || (i % 2 == 0)
            if (show) {
                smallTextPaint.textSize = sp(11f)
                smallTextPaint.color = Color.GRAY
                canvas.drawText(
                    "$prefix${i + 1}",
                    x,
                    chartBottom + dp(22f) + textBaselineOffset(smallTextPaint),
                    smallTextPaint
                )
            }
        }

        // 9) Título pequeño (opcional visual)
        textPaint.textSize = sp(13f)
        textPaint.color = Color.DKGRAY
        canvas.drawText(
            "Asistencias (tendencia)",
            w / 2f,
            dp(14f) + textBaselineOffset(textPaint),
            textPaint
        )
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
    private fun sp(v: Float): Float = v * resources.displayMetrics.scaledDensity

    private fun textBaselineOffset(paint: Paint): Float {
        val fm = paint.fontMetrics
        return -(fm.ascent + fm.descent) / 2f
    }
}
