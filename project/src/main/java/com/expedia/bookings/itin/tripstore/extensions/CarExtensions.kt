package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.CarLocation
import com.expedia.bookings.itin.tripstore.data.ItinCar

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
