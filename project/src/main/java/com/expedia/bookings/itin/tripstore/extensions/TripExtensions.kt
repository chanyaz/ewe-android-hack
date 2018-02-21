package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.data.Itin

fun Itin.firstHotel(): ItinHotel? {
    val packageHotels = packages?.first()?.hotels
    if (packageHotels != null && packageHotels.isNotEmpty()) {
        return packageHotels.first()
    } else if (hotels != null && hotels.isNotEmpty()) {
        return hotels.first()
    }
    return null
}
