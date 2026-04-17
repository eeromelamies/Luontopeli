package com.example.luontopeli.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationManager(context: Context) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints: StateFlow<List<GeoPoint>> = _routePoints.asStateFlow()

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _currentLocation.value = location
            // Lisätään reittipiste vain, jos sijainti on riittävän tarkka (esim. alle 20m)
            if (location.accuracy < 20) {
                val newPoint = GeoPoint(location.latitude, location.longitude)
                // Estetään tuplapisteet ja turha välkyntä
                if (_routePoints.value.isEmpty() || _routePoints.value.last() != newPoint) {
                    _routePoints.value = _routePoints.value + newPoint
                }
            }
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        try {
            // Haetaan viimeisin sijainti heti, jotta kartta ei ole tyhjä alussa
            val lastGps = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
            val lastNetwork = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            _currentLocation.value = lastGps ?: lastNetwork

            // Pyydetään päivityksiä molemmista lähteistä parhaan tuloksen saamiseksi
            val providers = listOf(
                android.location.LocationManager.GPS_PROVIDER,
                android.location.LocationManager.NETWORK_PROVIDER
            )

            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(
                        provider,
                        2000L, // 2 sekuntia
                        2f,    // 2 metriä (pienempi arvo = tasaisempi liike kartalla)
                        locationListener
                    )
                }
            }
        } catch (_: SecurityException) {}
    }

    fun stopTracking() {
        locationManager.removeUpdates(locationListener)
    }

    fun resetRoute() {
        _routePoints.value = emptyList()
    }

    fun calculateTotalDistance(): Float {
        val points = _routePoints.value
        if (points.size < 2) return 0f
        var total = 0f
        for (i in 0 until points.size - 1) {
            val results = FloatArray(1)
            Location.distanceBetween(
                points[i].latitude, points[i].longitude,
                points[i + 1].latitude, points[i + 1].longitude,
                results
            )
            total += results[0]
        }
        return total
    }
}
