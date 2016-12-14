package com.expedia.bookings.data.extensions

import com.expedia.bookings.data.LineOfBusiness

class LineOfBusinessExtensions {
    companion object {
         fun isUniversalCheckout(lob: LineOfBusiness): Boolean {
            return lob == LineOfBusiness.FLIGHTS_V2 || lob == LineOfBusiness.PACKAGES
        }
    }
}

fun LineOfBusiness.isMaterialLineOfBusiness(): Boolean {
    return this != LineOfBusiness.FLIGHTS && this != LineOfBusiness.ITIN
}

fun LineOfBusiness.isUniversalCheckout() : Boolean {
    return LineOfBusinessExtensions.isUniversalCheckout(this)
}
