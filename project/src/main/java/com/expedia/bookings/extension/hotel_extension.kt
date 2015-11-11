package com.expedia.bookings.extension

import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.pos.PointOfSale

public fun HotelRate.isShowAirAttached(): Boolean {
    return airAttached && PointOfSale.getPointOfSale().showHotelCrossSell() && isDiscountTenPercentOrBetter()
}

public fun shouldShowCircleForRatings(): Boolean {
    return PointOfSale.getPointOfSale().shouldShowCircleForRatings()
}
