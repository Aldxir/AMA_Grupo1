package com.example.alden

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
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
    }

    // TAREA 2: Definición de la función convencional
    private fun generarMensajeBienvenida(nombreUsuario: String): String {
        return "¡Bienvenido, $nombreUsuario, a la App Alden!"
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