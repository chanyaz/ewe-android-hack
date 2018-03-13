package com.expedia.bookings.itin.common

import com.google.android.gms.maps.model.LatLng

data class GoogleMapsLiteViewModel(
        val markerPositionsLatLng: List<LatLng>
)
