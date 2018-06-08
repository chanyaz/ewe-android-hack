package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.CarLocation
import com.expedia.bookings.itin.tripstore.data.ItinCar
import com.google.android.gms.maps.model.LatLng

fun CarLocation.buildSecondaryAddress(): String {
    val nullableArray = arrayOf(cityName, provinceStateName, countryCode, postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}

fun CarLocation.buildFullAddress(): String {
    val nullableArray = arrayOf(addressLine1, cityName, provinceStateName, countryCode, postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}

fun ItinCar.isDropOffSame(): Boolean {
    pickupLocation?.buildFullAddress()?.let { pickup ->
        dropOffLocation?.buildFullAddress()?.let { dropOff ->
            return pickup == dropOff
        }
    }
    return false
}

fun ItinCar.getLatLng(): LatLng? {
    val lat = this.dropOffLocation?.latitude
    val long = this.dropOffLocation?.longitude
    return if (lat != null && long != null) {
        LatLng(lat, long)
    } else {
        null
    }
}

fun ItinCar.getNameLocationPair(): Pair<String?, String?> {
    val name = this.carVendor?.longName
    val location = this.dropOffLocation?.buildSecondaryAddress()
    return Pair(name, location)
}
