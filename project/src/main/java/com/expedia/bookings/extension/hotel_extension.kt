package com.expedia.bookings.extension

import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.pos.PointOfSale

fun HotelRate.isShowAirAttached(): Boolean {
    return airAttached && PointOfSale.getPointOfSale().showHotelCrossSell() && isDiscountPercentNotZero() && PointOfSale.getPointOfSale().shouldShowAirAttach()
}

fun shouldShowCircleForRatings(): Boolean {
    return PointOfSale.getPointOfSale().shouldShowCircleForRatings()
}
