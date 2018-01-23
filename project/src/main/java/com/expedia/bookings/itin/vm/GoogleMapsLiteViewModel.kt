package com.expedia.bookings.itin.vm

import com.google.android.gms.maps.model.LatLng

data class GoogleMapsLiteViewModel(
        val markerPositionsLatLng: List<LatLng>
)
