package com.example.alden.charts.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.random.Random

/**
 * StatsStore
 * - Guarda totales: asistencias / inasistencias
 * - Guarda serie por día: YYYY-MM-DD -> conteo (asistencias)
 *
 * Cumple el requisito de la práctica: persistencia desde SharedPreferences.
 */
class StatsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    data class Totals(
        val asistencias: Int,
        val inasistencias: Int
    ) {
        val total: Int get() = asistencias + inasistencias
        val porcentajeAsistencia: Float
            get() = if (total == 0) 0f else asistencias.toFloat() / total.toFloat()
    }

    fun getTotals(): Totals {
        val ok = prefs.getInt(KEY_ASISTENCIAS, 0)
        val no = prefs.getInt(KEY_INASISTENCIAS, 0)
        return Totals(ok, no)
    }

    fun addAsistencia() {
        prefs.edit()
            .putInt(KEY_ASISTENCIAS, prefs.getInt(KEY_ASISTENCIAS, 0) + 1)
            .apply()

        // además incrementa el conteo diario para el gráfico de línea
        val todayKey = LocalDate.now().format(dateFmt)
        val dayKey = "${KEY_DAY_PREFIX}$todayKey"
        val current = prefs.getInt(dayKey, 0)
        prefs.edit().putInt(dayKey, current + 1).apply()
    }

    fun addInasistencia() {
        prefs.edit()
            .putInt(KEY_INASISTENCIAS, prefs.getInt(KEY_INASISTENCIAS, 0) + 1)
            .apply()
    }

    /**
     * Retorna lista de "asistencias" para los últimos [days] días.
     * Ej: days=7 -> 7 puntos (de más antiguo a más reciente).
     */
    fun getAsistenciasLastDays(days: Int): List<Int> {
        val safeDays = max(1, days)
        val today = LocalDate.now()
        val out = ArrayList<Int>(safeDays)

        for (i in (safeDays - 1) downTo 0) {
            val d = today.minusDays(i.toLong())
            val key = "${KEY_DAY_PREFIX}${d.format(dateFmt)}"
            out.add(prefs.getInt(key, 0))
        }
        return out
    }

    /**
     * Agrupa en "semanas" simples (buckets) tomando los últimos 7*weeks días y sumando
     * de 7 en 7. Ej: weeks=4 -> 4 puntos.
     */
    fun getAsistenciasByWeekBuckets(weeks: Int): List<Int> {
        val safeWeeks = max(1, weeks)
        val days = safeWeeks * 7
        val daily = getAsistenciasLastDays(days)
        val out = ArrayList<Int>(safeWeeks)

        var idx = 0
        while (idx < daily.size) {
            val bucket = daily.subList(idx, minOf(idx + 7, daily.size)).sum()
            out.add(bucket)
            idx += 7
        }
        return out
    }

    fun clearAll() {
        val editor = prefs.edit()
        editor.remove(KEY_ASISTENCIAS)
        editor.remove(KEY_INASISTENCIAS)

        // Borra series guardadas por día (todas las keys que empiezan con day_)
        val all = prefs.all.keys
        for (k in all) {
            if (k.startsWith(KEY_DAY_PREFIX)) editor.remove(k)
        }
        editor.apply()
    }

    /**
     * Crea datos demo: llena 30 días atrás con valores aleatorios y ajusta totales.
     * Esto sirve para pruebas rápidas (requisito: probar distintos conjuntos y datos vacíos).
     */
    fun seedDemoData() {
        clearAll()

        val today = LocalDate.now()
        var asist = 0
        var inasist = 0

        val editor = prefs.edit()

        // 30 días de ejemplo: asistencia diaria entre 0..8
        for (i in 29 downTo 0) {
            val d = today.minusDays(i.toLong())
            val value = Random.nextInt(0, 9) // 0..8
            val key = "${KEY_DAY_PREFIX}${d.format(dateFmt)}"
            editor.putInt(key, value)
            asist += value
        }

        // Inasistencias demo: proporcional pero menor
        inasist = max(0, asist / 4)

        editor.putInt(KEY_ASISTENCIAS, asist)
        editor.putInt(KEY_INASISTENCIAS, inasist)
        editor.apply()
    }

    companion object {
        private const val PREFS_NAME = "charts_stats_prefs"
        private const val KEY_ASISTENCIAS = "asistencias"
        private const val KEY_INASISTENCIAS = "inasistencias"
        private const val KEY_DAY_PREFIX = "day_" // day_YYYY-MM-DD
    }
}
