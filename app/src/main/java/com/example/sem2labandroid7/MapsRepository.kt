package com.example.sem2labandroid7

import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode

class MapsRepository(private val geoApiContext: GeoApiContext) {
    suspend fun getRoute(
        start: LatLng,
        end: LatLng,
        mode: TravelMode
    ): List<LatLng> {
        val result = DirectionsApi.newRequest(geoApiContext)
            .mode(mode)
            .origin(com.google.maps.model.LatLng(start.latitude, start.longitude))
            .destination(com.google.maps.model.LatLng(end.latitude, end.longitude))
            .await()

        return result.routes.first().overviewPolyline.decodePath()
            .map { LatLng(it.lat, it.lng) }
    }
}

