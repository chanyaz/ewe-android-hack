package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.ItinLx

fun ItinLx.buildSecondaryAddress(): String {
    val nullableArray = arrayOf(activityLocation?.city, activityLocation?.countrySubdivisionCode, activityLocation?.countryCode, activityLocation?.postalCode)
    val nonNullArray = nullableArray.filterNotNull()
    return nonNullArray.joinToString()
}

fun ItinLx.buildFullAddress(): String {
    val nullableArray = arrayOf(activityLocation?.addressLine1, activityLocation?.city, activityLocation?.countrySubdivisionCode, activityLocation?.countryCode, activityLocation?.postalCode)
    val nonNullArray = nullableArray.filterNotNull()
    return nonNullArray.joinToString()
}
