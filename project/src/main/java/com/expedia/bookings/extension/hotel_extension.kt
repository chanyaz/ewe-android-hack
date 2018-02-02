package com.expedia.bookings.extension

import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.pos.PointOfSale

fun HotelRate.isShowAirAttached(): Boolean {
    return airAttached && isDiscountPercentNotZero
}

fun shouldShowCircleForRatings(): Boolean {
    return PointOfSale.getPointOfSale().shouldShowCircleForRatings()
}
