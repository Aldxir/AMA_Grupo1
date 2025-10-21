package com.example.alden

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.alden.data.AccessRepository
import com.example.alden.data.UserRepository
import com.example.alden.models.Accion
import com.example.alden.models.RegistroAcceso
import com.example.alden.models.Ubicacion
import com.example.alden.rules.displayName
import com.example.alden.services.AttendanceService
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // Declarar variables con al menos 5 tipos diferentes.
        Log.d("TAREAS_ALDEN", "--- Tarea 1: Variables ---")
        val nombreApp: String = "Alden"
        var version: Int = 2
        val pi: Double = 3.14159
        val esVersionFinal: Boolean = false
        val desarrolladores: List<String> = listOf("Aldair", "Dennys")

        Log.d("TAREAS_ALDEN", "Nombre de la App: $nombreApp")
        Log.d("TAREAS_ALDEN", "Versión: $version")
        Log.d("TAREAS_ALDEN", "Valor de PI: $pi")
        Log.d("TAREAS_ALDEN", "Es versión final: $esVersionFinal")
        Log.d("TAREAS_ALDEN", "Equipo: ${desarrolladores.joinToString()}")

        //Implementar una función convencional.
        Log.d("TAREAS_ALDEN", "--- Tarea 2: Función Convencional ---")
        val bienvenida = generarMensajeBienvenida("Aldair")
        Log.d("TAREAS_ALDEN", bienvenida)

        // Crear una clase con al menos una propiedad y un metodo.
        Log.d("TAREAS_ALDEN", "--- Tarea 3: Clase Tradicional (Proyecto) ---")
        val miProyecto = Proyecto("Calculadora de Edad")
        miProyecto.mostrarEstado()

        // Crear una data class y usar extension functions.
        Log.d("TAREAS_ALDEN", "--- Tarea 4: Data Class y Extensiones ---")
        val usuario2 = Usuario(nombre = "Dennys Perez", correo = "dennys@email.com", edad = 15)
        val usuario1 = Usuario(nombre = "Aldair Flor", correo = "aldair@email.com", edad = 25)

        // Usando la extension function para formatear nombre
        Log.d("TAREAS_ALDEN", usuario1.nombreFormateado())
        // ESTE ES EL CÓDIGO NUEVO Y MEJORADO
        Log.d("TAREAS_ALDEN", "El usuario ${usuario1.nombre} (${usuario1.edad} años) ¿es mayor de edad?: ${usuario1.esMayorDeEdad()}")
        Log.d("TAREAS_ALDEN", "El usuario ${usuario2.nombre} (${usuario2.edad} años) ¿es mayor de edad?: ${usuario2.esMayorDeEdad()}")




        val txtFecha = findViewById<EditText>(R.id.txtFecha)
        val btnCalcular = findViewById<Button>(R.id.btnCalcular)
        val txtResultado = findViewById<TextView>(R.id.txtResultado)

        btnCalcular.setOnClickListener {
            val fechaIngresada = txtFecha.text.toString()
            if (fechaIngresada.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaNac: Date = sdf.parse(fechaIngresada)!!
                    val hoy = Calendar.getInstance()
                    val nacimiento = Calendar.getInstance()
                    nacimiento.time = fechaNac

                    var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)

                    if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
                        edad--
                    }

                    txtResultado.text = "Edad: $edad años"
                } catch (e: Exception) {
                    txtResultado.text = "Formato incorrecto (use dd/MM/yyyy)"
                }
            } else {
                txtResultado.text = "Ingrese una fecha válida"
            }
        }


        // ========================= Practica 3 =====================================
        Log.d("Practica 3", "=== Sistema de Asistencias (demo de práctica) ===")
        Log.d("", "Elige perfil:\n 1) Admin\n 2) User")
        when (readLine()?.trim()) {
            "1" -> flujoAdmin()
            "2" -> flujoUser()
            else -> {
                println("Opción no válida. Ejecutando PRUEBAS automáticas...")
                ejecutarPruebas()
            }
        }


    }

    // TAREA 2: Definición de la función convencional
    private fun generarMensajeBienvenida(nombreUsuario: String): String {
        return "¡Bienvenido, $nombreUsuario, a la App Alden!"
    }


    // =================== Practica 3 ==========================
    /** Flujo Admin: solo listar, no registra. */
    private fun flujoAdmin() {
        val admin = UserRepository.getAdmin()
        Log.d("","Bienvenido, ${admin.displayName()} (ADMIN). No está permitido registrar asistencias como Admin.")
        Log.d("", "Registros existentes:")
        val registros = AccessRepository.all()
        if (registros.isEmpty()) println("  (No hay registros aún)")
        else registros.forEach { println(renderRegistro(it)) }

        // Además, ejecutamos las pruebas para que el Admin pueda ver evidencias rápidas.
        Log.d("", "\n--- Ejecutando PRUEBAS para evidencias ---")
        ejecutarPruebas()
    }

    /** Flujo User: puede registrar ENTRADA/SALIDA y listar sus propios registros (válidos). */
    private fun flujoUser() {
        val user = UserRepository.getAnyEnabledUser()
        println("Bienvenido, ${user.displayName()} (USER habilitado).")

        val sc = Scanner(System.`in`)
        loop@ while (true) {
            Log.d("", "\nElige acción:")
            Log.d("", " 1) Registrar ENTRADA")
            Log.d("", " 2) Registrar SALIDA")
            Log.d("", " 3) Ver mis registros")
            Log.d("", " 4) Salir")
            val input = sc.nextLine().trim()
            when (input) {
                "1", "2" -> {
                    val accion = if (input == "1") Accion.ENTRADA else Accion.SALIDA
                    val ubicacion = pedirUbicacion(sc)
                    val now = LocalDateTime.now()
                    val registro = RegistroAcceso(user, accion, ubicacion, now)
                    val ok = AttendanceService.registrarSiHabilitado(registro)
                    println(if (ok) "HABILITADO" else "DESHABILITADO")
                }
                "3" -> {
                    println("Mis registros validados por política:")
                    val mios = AccessRepository.byUser(user.id)
                    if (mios.isEmpty()) println("  (Sin registros aún)")
                    else mios.forEach { println(renderRegistro(it)) }
                }
                "4" -> break@loop
                else -> println("Opción no válida.")
            }
        }
    }

    /** Pide ubicación simple por consola. */
    private fun pedirUbicacion(sc: Scanner): Ubicacion {
        while (true) {
            Log.d("", "Indica ubicación:\n 1) Dentro del rango\n 2) Fuera del rango")
            when (sc.nextLine().trim()) {
                "1" -> return Ubicacion.DENTRO_RANGO
                "2" -> return Ubicacion.FUERA_RANGO
            }
            Log.d("", "Opción no válida.")
        }
    }

    private fun renderRegistro(r: RegistroAcceso): String =
        "- ${r.usuario.displayName()} | ${r.accion} | ${r.ubicacion} | ${r.hora}"

    /**
     * PRUEBAS pedidas (imprime evidencias en consola):
     *  1) User habilitado + dentro de horario + dentro del rango ⇒ HABILITADO
     *  2) Fuera de horario ⇒ DESHABILITADO
     *  3) Ubicación fuera del rango ⇒ DESHABILITADO
     *  4) Admin visualiza todos los registros y no registra asistencias
     */
    private fun ejecutarPruebas() {
        val admin = UserRepository.getAdmin()
        val user = UserRepository.getAnyEnabledUser()

        // Limpieza mínima: no eliminamos datos, solo informamos separadores en salida.
        println("\n[PRUEBA 1] User habilitado + horario válido + ubicación válida ⇒ HABILITADO")
        val t1 = LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0) // 10:00
        val r1 = RegistroAcceso(user, Accion.ENTRADA, Ubicacion.DENTRO_RANGO, t1)
        val ok1 = AttendanceService.registrarSiHabilitado(r1)
        println(if (ok1) "HABILITADO" else "DESHABILITADO")

        println("\n[PRUEBA 2] Fuera de horario (05:30) ⇒ DESHABILITADO")
        val t2 = LocalDateTime.now().withHour(5).withMinute(30).withSecond(0).withNano(0) // 05:30
        val r2 = RegistroAcceso(user, Accion.SALIDA, Ubicacion.DENTRO_RANGO, t2)
        val ok2 = AttendanceService.registrarSiHabilitado(r2)
        println(if (ok2) "HABILITADO" else "DESHABILITADO")

        println("\n[PRUEBA 3] Ubicación fuera del rango ⇒ DESHABILITADO")
        val t3 = LocalDateTime.now().withHour(10).withMinute(15).withSecond(0).withNano(0)
        val r3 = RegistroAcceso(user, Accion.ENTRADA, Ubicacion.FUERA_RANGO, t3)
        val ok3 = AttendanceService.registrarSiHabilitado(r3)
        println(if (ok3) "HABILITADO" else "DESHABILITADO")

        println("\n[PRUEBA 4] Admin visualiza todos los registros y no registra asistencias")
        println("Admin: ${admin.displayName()} (solo listado; no puede registrar por flujo).")
        val registros = AccessRepository.all()
        if (registros.isEmpty()) {
            println("  (No hay registros válidos aún)")
        } else {
            registros.forEach { println(renderRegistro(it)) }
        }

        // Extra: muestra franja horaria válida para claridad
        println("\n* Nota: horario permitido = ${LocalTime.of(6,0)} a ${LocalTime.of(20,0)} (inclusive).")
    }

}


// TAREA 3: Definición de la clase tradicional
class Proyecto(val nombre: String) {
    fun mostrarEstado() {
        Log.d("TAREAS_ALDEN", "El proyecto '$nombre' está en desarrollo.")
    }
}

// TAREA 4: Definición de la data class
data class Usuario(val nombre: String, val correo: String, val edad: Int)

// TAREA 4: Extension functions para la clase Usuario
fun Usuario.nombreFormateado(): String {
    return "Nombre de usuario: ${this.nombre.uppercase()}"
}

fun Usuario.esMayorDeEdad(): Boolean {
    return this.edad >= 18
}