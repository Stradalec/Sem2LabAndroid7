package com.example.sem2labandroid7

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(getString(R.string.mapkit_api_key))
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapview)
        mapView.map.move(
            CameraPosition(
                Point(55.354993, 86.085805), 15.0f, 0.0f, 0.0f
            )
        )
        val cameraListener = CameraListener { _, _, _, _ ->
        }
        mapView.mapWindow.map.addCameraListener(cameraListener)
        val imageProvider = ImageProvider.fromResource(this, R.drawable.ic_launcher_foreground)
        val placemark = mapView.map.mapObjects.addPlacemark().apply {
            geometry = Point(55.354993, 86.085805)
            setIcon(imageProvider)
        }
        placemark.addTapListener(placemarkTapListener)
        val inputListener = object : InputListener {
            override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {

            }

            override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
                val centerX = mapView.mapWindow.width() / 2f
                val centerY = mapView.mapWindow.height() / 2f
                val centerPoint = ScreenPoint(centerX, centerY)
                val worldPoint = mapView.mapWindow.screenToWorld(centerPoint)
                map.mapObjects.addPlacemark().apply {
                    geometry = worldPoint!!
                    setIcon(ImageProvider.fromResource(this@MainActivity, R.drawable.ic_launcher_foreground))
                }
            }
        }
        mapView.map.addInputListener(inputListener)

    }
    private val placemarkTapListener = MapObjectTapListener { _, point ->
        Toast.makeText(
            this@MainActivity,
            "Tapped the point (${point.longitude}, ${point.latitude})",
            Toast.LENGTH_SHORT
        ).show()
        true
    }
    private  fun createRoute(){
        val drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.ONLINE)

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