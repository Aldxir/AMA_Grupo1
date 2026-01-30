package com.example.alden.ui.location

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.alden.R
import com.example.alden.accesscontrol.ScreenGuard
import com.example.alden.di.Singletons
import com.example.alden.models.Ubicacion
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeolocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // UI
    private lateinit var tvDistance: TextView
    private lateinit var tvStatus: TextView

    // CONFIGURACIÓN DE LA GEOVALLA (Ej: Un punto en el campus)
    // Puedes cambiar estas coordenadas por las de tu universidad real
    private val TARGET_LAT = -0.210384 // Latitud EPN (Ejemplo)
    private val TARGET_LNG = -78.488737 // Longitud EPN (Ejemplo)
    private val GEOFENCE_RADIUS = 100.0 // Metros

    private var userMarker: Marker? = null
    private var routeLine: Polyline? = null

    // Permisos
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            enableMyLocation()
        } else {
            Toast.makeText(this, "Se requieren permisos de ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geolocation)

        // 1. Seguridad: Validar Sesión
        val session = Singletons.session.getSessionInfo()
        if (!session.isLoggedIn) {
            ScreenGuard.redirectToLogin(this)
            return
        }

        // 2. Inicializar Vistas
        tvDistance = findViewById(R.id.tvDistance)
        tvStatus = findViewById(R.id.tvStatus)

        // 3. Inicializar Servicios de Mapa y Ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Configurar UI del mapa
        map.uiSettings.isZoomControlsEnabled = true

        // Dibujar la Geovalla (Zona permitida)
        drawGeofence()

        // Verificar permisos e iniciar rastreo
        checkPermissions()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun enableMyLocation() {
        try {
            map.isMyLocationEnabled = true // Muestra el punto azul nativo
            startLocationUpdates()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun drawGeofence() {
        val target = LatLng(TARGET_LAT, TARGET_LNG)

        // 1. Dibujar Círculo
        map.addCircle(CircleOptions()
            .center(target)
            .radius(GEOFENCE_RADIUS)
            .strokeColor(Color.BLUE)
            .fillColor(0x220000FF) // Azul transparente
            .strokeWidth(2f)
        )

        // 2. Dibujar Marcador del Aula
        map.addMarker(MarkerOptions()
            .position(target)
            .title("Punto de Asistencia")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

        // Mover cámara inicialmente al objetivo
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 16f))
    }

    private fun startLocationUpdates() {
        // En una app real usaríamos LocationCallback para actualizaciones continuas.
        // Para esta práctica, obtenemos la última conocida y forzamos actualización simple.
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    location?.let { updateUI(it) }
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun updateUI(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        val targetLatLng = LatLng(TARGET_LAT, TARGET_LNG)

        // 1. Calcular Distancia (Matemática esférica)
        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude, location.longitude,
            TARGET_LAT, TARGET_LNG,
            results
        )
        val distanceInMeters = results[0]

        // 2. Actualizar Textos
        tvDistance.text = "Distancia: ${"%.2f".format(distanceInMeters)} metros"

        // 3. Validar Geovalla y Actualizar Estado Global
        val isInside = distanceInMeters <= GEOFENCE_RADIUS

        if (isInside) {
            tvStatus.text = "ESTADO: DENTRO DE ZONA (Permitido)"
            tvStatus.setTextColor(Color.GREEN)
            // ACTUALIZAR EL SINGLETON PARA QUE EL DASHBOARD HABILITE BOTONES
            CoroutineScope(Dispatchers.Main).launch {
                Singletons.location.setZone(Ubicacion.DENTRO_RANGO)
            }
        } else {
            tvStatus.text = "ESTADO: FUERA DE ZONA (Bloqueado)"
            tvStatus.setTextColor(Color.RED)
            // BLOQUEAR EN EL SINGLETON
            CoroutineScope(Dispatchers.Main).launch {
                Singletons.location.setZone(Ubicacion.FUERA_RANGO)
            }
        }

        // 4. Dibujar Ruta (Línea recta dinámica)
        if (routeLine != null) routeLine?.remove()
        routeLine = map.addPolyline(PolylineOptions()
            .add(userLatLng, targetLatLng)
            .width(5f)
            .color(Color.DKGRAY)
            .geodesic(true))

        // Mover cámara para ver ambos puntos
        val builder = LatLngBounds.Builder()
        builder.include(userLatLng)
        builder.include(targetLatLng)
        try {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
        } catch (e: Exception) {
            // Fallback si el mapa no ha cargado dimensiones
            map.animateCamera(CameraUpdateFactory.newLatLng(userLatLng))
        }
    }
}