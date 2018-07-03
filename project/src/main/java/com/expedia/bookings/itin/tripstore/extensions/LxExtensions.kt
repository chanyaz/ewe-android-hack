package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.google.android.gms.maps.model.LatLng

fun ItinLx.buildSecondaryAddress(): String {
    val nullableArray = arrayOf(redemptionLocations?.firstOrNull()?.city, redemptionLocations?.firstOrNull()?.countrySubdivisionCode, redemptionLocations?.firstOrNull()?.countryCode, redemptionLocations?.firstOrNull()?.postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}

fun ItinLx.buildFullAddress(): String {
    val nullableArray = arrayOf(redemptionLocations?.firstOrNull()?.addressLine1, redemptionLocations?.firstOrNull()?.city, redemptionLocations?.firstOrNull()?.countrySubdivisionCode, redemptionLocations?.firstOrNull()?.countryCode, redemptionLocations?.firstOrNull()?.postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}

fun ItinLx.getLatLng(): LatLng? {
    val lat = redemptionLocations?.firstOrNull()?.latitude
    val long = redemptionLocations?.firstOrNull()?.longitude
    return if (lat != null && long != null) {
        LatLng(lat, long)
    } else {
        null
    }
}

fun ItinLx.getNameLocationPair(): Pair<String?, String?> {
    val name = redemptionLocations?.firstOrNull()?.name1
    val location = buildSecondaryAddress()
    return Pair(name, location)
}
