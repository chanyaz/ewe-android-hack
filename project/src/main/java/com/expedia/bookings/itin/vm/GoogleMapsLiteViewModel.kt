package com.expedia.bookings.itin.vm

import com.google.android.gms.maps.model.LatLng

data class GoogleMapsLiteViewModel(
        val cameraPositionLatLng: LatLng,
        val markerPositionsLatLng: List<LatLng>
)