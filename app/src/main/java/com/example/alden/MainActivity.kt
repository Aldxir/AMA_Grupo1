package com.example.alden

import android.os.Bundle
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

                    // Ajustar si aún no cumplió años este año
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
}