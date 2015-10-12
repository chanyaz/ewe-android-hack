package com.expedia.bookings.extension

import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.pos.PointOfSale

public fun HotelRate.isAirAttached(): Boolean {
    return airAttached && PointOfSale.getPointOfSale().showHotelCrossSell() && isDiscountTenPercentOrBetter()
}