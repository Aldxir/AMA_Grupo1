package com.example.alden.charts.ui

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.alden.R
import com.example.alden.accesscontrol.ScreenGuard
import com.example.alden.di.Singletons
import com.example.alden.charts.views.BarChartView
import com.example.alden.charts.views.LineChartView
import com.example.alden.charts.views.PieChartView

class ChartsActivity : AppCompatActivity() {

    private lateinit var pie: PieChartView
    private lateinit var bar: BarChartView
    private lateinit var line: LineChartView
    private lateinit var tvResumen: TextView
    private lateinit var rgRange: RadioGroup
    private lateinit var rbSemana: RadioButton
    private lateinit var rbMes: RadioButton
    private lateinit var btnDemo: Button
    private lateinit var btnClear: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)

        // 1) Protección: sin sesión -> Login
        val session = Singletons.session.getSessionInfo()
        if (!session.isLoggedIn) {
            ScreenGuard.redirectToLogin(this)
            return
        }

        title = "Estadísticas"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 2) UI refs
        pie = findViewById(R.id.pieChart)
        bar = findViewById(R.id.barChart)
        line = findViewById(R.id.lineChart)
        tvResumen = findViewById(R.id.tvResumen)
        rgRange = findViewById(R.id.rgRange)
        rbSemana = findViewById(R.id.rbSemana)
        rbMes = findViewById(R.id.rbMes)
        btnDemo = findViewById(R.id.btnDemoData)
        btnClear = findViewById(R.id.btnClearData)

        // 3) Listeners
        rgRange.setOnCheckedChangeListener { _, _ -> renderAll(animate = true) }
        btnDemo.setOnClickListener {
            Singletons.statsStore.seedDemoData()
            renderAll(animate = true)
        }
        btnClear.setOnClickListener {
            Singletons.statsStore.clearAll()
            renderAll(animate = true)
        }

        // 4) Render inicial
        if (rgRange.checkedRadioButtonId == -1) rbSemana.isChecked = true
        renderAll(animate = true)
    }

    override fun onResume() {
        super.onResume()
        // refresca por si hubo nuevos registros
        renderAll(animate = false)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun renderAll(animate: Boolean) {
        val totals = Singletons.statsStore.getTotals()

        // Pie: porcentaje de asistencia (ok / (ok + no))
        pie.setData(totals.asistencias, totals.inasistencias, animate)

        // Barras: comparación directa
        bar.setData(totals.asistencias, totals.inasistencias, animate)

        // Línea: semana (7 días) o mes (4 semanas)
        val series = if (rbSemana.isChecked) {
            Singletons.statsStore.getAsistenciasLastDays(days = 7)
        } else {
            Singletons.statsStore.getAsistenciasByWeekBuckets(weeks = 4)
        }
        line.setSeries(series, animate)

        val percent = (totals.porcentajeAsistencia * 100f).toInt()
        tvResumen.text =
            "Asistencias: ${totals.asistencias}   |   Inasistencias: ${totals.inasistencias}   |   %: ${percent}%"
    }
}
