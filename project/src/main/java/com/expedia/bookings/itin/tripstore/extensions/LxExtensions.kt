package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.google.android.gms.maps.model.LatLng

fun ItinLx.buildSecondaryAddress(): String {
    val nullableArray = arrayOf(activityLocation?.city, activityLocation?.countrySubdivisionCode, activityLocation?.countryCode, activityLocation?.postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}

fun ItinLx.buildFullAddress(): String {
    val nullableArray = arrayOf(activityLocation?.addressLine1, activityLocation?.city, activityLocation?.countrySubdivisionCode, activityLocation?.countryCode, activityLocation?.postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}

fun ItinLx.getLatLng(): LatLng? {
    val lat = activityLocation?.latitude
    val long = activityLocation?.longitude
    return if (lat != null && long != null) {
        LatLng(lat, long)
    } else {
        null
    }
}

fun ItinLx.getNameLocationPair(): Pair<String?, String?> {
    val name = activityLocation?.name1
    val location = buildSecondaryAddress()
    return Pair(name, location)
}
