package com.example.sem2labandroid7

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MapViewModel: ViewModel() {
    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: LiveData<LatLng> = _currentLocation

    private val _requestLocationPermission = MutableLiveData<Boolean>()
    val requestLocationPermission: LiveData<Boolean> = _requestLocationPermission
    val defaultLocation = LatLng(55.354993, 86.085805)

    fun updateCurrentLocation(location: LatLng) {
        _currentLocation.value = location
    }

    fun requestLocationUpdates() {
        _requestLocationPermission.value = true
    }
}