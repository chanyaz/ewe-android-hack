package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.google.android.gms.maps.model.LatLng

fun ItinLx.buildSecondaryAddress(): String {
    val redemptionLocation = redemptionLocations?.firstOrNull()
    val nullableArray = arrayOf(redemptionLocation?.city, redemptionLocation?.countrySubdivisionCode, redemptionLocation?.countryCode, redemptionLocation?.postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}

fun ItinLx.buildFullAddress(): String {
    val redemptionLocation = redemptionLocations?.firstOrNull()
    val nullableArray = arrayOf(redemptionLocation?.addressLine1, redemptionLocation?.city, redemptionLocation?.countrySubdivisionCode, redemptionLocation?.countryCode, redemptionLocation?.postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}

fun ItinLx.getLatLng(): LatLng? {
    val redemptionLocation = redemptionLocations?.firstOrNull()
    val lat = redemptionLocation?.latitude
    val long = redemptionLocation?.longitude
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
