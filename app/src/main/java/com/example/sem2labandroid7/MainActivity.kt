package com.example.sem2labandroid7

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity(), OnMapReadyCallback, OnMapLongClickListener {
    private lateinit var map: GoogleMap
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var routePolyline: Polyline? = null
    private lateinit var geoApiContext: GeoApiContext
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel: MapViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        geoApiContext = GeoApiContext.Builder()
            .apiKey("MAPS_API_KEY")
            .build()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        viewModel.currentLocation.observe(this) { location ->
            zoomTo(location)
        }
        viewModel.requestLocationPermission.observe(this) { shouldRequest ->
            if (shouldRequest) {
                checkLocationPermissions()
            }
        }
        findViewById<Button>(R.id.btn_zoom_to_location).setOnClickListener {
            zoomTo(viewModel.defaultLocation)
        }

        findViewById<Button>(R.id.btn_zoom_to_user).setOnClickListener {
            viewModel.requestLocationUpdates()
        }
        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            clearMarkersAndRoute()
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    private fun clearMarkersAndRoute() {
        startMarker?.remove()
        endMarker?.remove()
        routePolyline?.remove()

        startMarker = null
        endMarker = null
        routePolyline = null
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMapLongClickListener(this)
    }
    private fun zoomTo(position: LatLng) {
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                position,
                15f
            )
        )
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            fetchLocation()
        }
    }
    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val userLocation = LatLng(it.latitude, it.longitude)
                        viewModel.updateCurrentLocation(userLocation)
                    }
                }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation()
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onMapLongClick(latLng: LatLng) {
        if (startMarker == null) {
            startMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Старт")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
        } else {
            endMarker?.remove()
            endMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Финиш")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
            startMarker?.position?.let { start ->
                endMarker?.position?.let { end ->
                    drawRoute(start, end)
                }
            }
        }
    }
    private fun drawRoute(start: LatLng, end: LatLng) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val directionsResult = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.WALKING)
                    .origin(com.google.maps.model.LatLng(start.latitude, start.longitude))
                    .destination(com.google.maps.model.LatLng(end.latitude, end.longitude))
                    .await()

                if (directionsResult.routes.isNotEmpty()) {
                    val points = directionsResult.routes[0]
                        .overviewPolyline
                        .decodePath()
                        .map { LatLng(it.lat, it.lng) }

                    withContext(Dispatchers.Main) {
                        routePolyline?.remove()
                        routePolyline = map.addPolyline(
                            PolylineOptions()
                                .addAll(points)
                                .color(Color.BLUE)
                                .width(12f)
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        geoApiContext.shutdown()
    }

}