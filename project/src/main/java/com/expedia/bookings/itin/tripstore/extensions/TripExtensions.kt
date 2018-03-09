package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.data.Itin

fun Itin.firstHotel(): ItinHotel? {
    val packageHotels = packages?.first()?.hotels ?: emptyList()
    val standAloneHotels = hotels.orEmpty()
    return packageHotels.plus(standAloneHotels).firstOrNull()
}
