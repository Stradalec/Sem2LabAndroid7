package com.example.sem2labandroid7

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private lateinit var repository: MapsRepository


    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: LiveData<LatLng> = _currentLocation

    private val _requestLocationPermission = MutableLiveData<Boolean>()
    val requestLocationPermission: LiveData<Boolean> = _requestLocationPermission
    val defaultLocation = LatLng(55.354993, 86.085805)

    private val _routePoints = MutableLiveData<List<LatLng>>()
    val routePoints: LiveData<List<LatLng>> = _routePoints

    private val _routeError = MutableLiveData<String?>()
    val routeError: LiveData<String?> = _routeError

    private val _markers = MutableLiveData<Pair<LatLng?, LatLng?>>(Pair(null, null))
    val markers: LiveData<Pair<LatLng?, LatLng?>> = _markers

    fun updateCurrentLocation(location: LatLng) {
        _currentLocation.value = location
    }

    fun requestLocationUpdates() {
        _requestLocationPermission.value = true
    }

    fun initRepository(geoApiContext: GeoApiContext) {
        repository = MapsRepository(geoApiContext)
    }

    fun calculateRoute(start: LatLng, end: LatLng) {
        viewModelScope.launch {
            try {
                _routePoints.postValue(repository.getRoute(start, end, TravelMode.WALKING))
            } catch (e: Exception) {
                _routeError.postValue(e.message ?: "Unknown error")
            }
        }
    }

    fun updateMarkers(start: LatLng?, end: LatLng?) {
        _markers.postValue(Pair(start, end))
    }
}