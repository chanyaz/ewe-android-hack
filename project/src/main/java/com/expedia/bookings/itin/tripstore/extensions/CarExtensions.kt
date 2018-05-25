package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.ItinCar

fun ItinCar.buildPickupSecondaryAddress(): String {
    val nullableArray = arrayOf(pickupLocation?.cityName, pickupLocation?.provinceStateName, pickupLocation?.countryCode, pickupLocation?.postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}

fun ItinCar.buildDropOffSecondaryAddress(): String {
    val nullableArray = arrayOf(dropOffLocation?.cityName, dropOffLocation?.provinceStateName, dropOffLocation?.countryCode, dropOffLocation?.postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}

fun ItinCar.buildFullPickupAddress(): String {
    val nullableArray = arrayOf(pickupLocation?.addressLine1, pickupLocation?.cityName, pickupLocation?.provinceStateName, pickupLocation?.countryCode, pickupLocation?.postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}

fun ItinCar.buildFullDropOffAddress(): String {
    val nullableArray = arrayOf(dropOffLocation?.addressLine1, dropOffLocation?.cityName, dropOffLocation?.provinceStateName, dropOffLocation?.countryCode, dropOffLocation?.postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}