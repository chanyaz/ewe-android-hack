package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.pos.PointOfSale

fun shouldShowCircleForRatings(): Boolean {
    return PointOfSale.getPointOfSale().shouldShowCircleForRatings()
}
