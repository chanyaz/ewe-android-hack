package com.expedia.bookings.data.extensions

import com.expedia.bookings.data.LineOfBusiness

fun LineOfBusiness.isMaterialLineOfBusiness(): Boolean {
    return this != LineOfBusiness.FLIGHTS && this != LineOfBusiness.ITIN
}
