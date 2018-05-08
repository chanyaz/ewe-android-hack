package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.ItinLx

fun ItinLx.buildSecondaryAddress(): String {
    val builder = arrayOf(activityLocation?.city, activityLocation?.countrySubdivisionCode, activityLocation?.countryCode, activityLocation?.postalCode)
    val nonNullArray = builder.filterNotNull()
    return nonNullArray.joinToString()
}