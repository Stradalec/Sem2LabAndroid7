package com.example.sem2labandroid7

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeMapKit()
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapview)
        mapView.getMap().move(
            CameraPosition(
                Point(55.354993, 86.085805), 15.0f, 0.0f, 0.0f
            )
        )

    }
    private fun initializeMapKit() {
        try {
            MapKitFactory.setApiKey(getString(R.string.mapkit_api_key))
            MapKitFactory.initialize(this)
        } catch (e: Exception) {
            Log.e("MAPKIT", "Ошибка инициализации", e)
            Toast.makeText(this, "Ошибка карт: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}