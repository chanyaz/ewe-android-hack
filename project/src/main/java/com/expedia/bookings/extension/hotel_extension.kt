package com.expedia.bookings.extension

import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration

fun HotelRate.isShowAirAttached(): Boolean {
    return airAttached && isDiscountPercentNotZero && ProductFlavorFeatureConfiguration.getInstance().shouldShowAirAttach()
}

fun shouldShowCircleForRatings(): Boolean {
    return PointOfSale.getPointOfSale().shouldShowCircleForRatings()
}
