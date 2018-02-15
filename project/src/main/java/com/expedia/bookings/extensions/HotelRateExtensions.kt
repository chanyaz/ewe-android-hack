package com.expedia.bookings.extensions

import com.expedia.bookings.data.hotels.HotelRate

fun HotelRate.isShowAirAttached(): Boolean {
    return airAttached && isDiscountPercentNotZero
}
