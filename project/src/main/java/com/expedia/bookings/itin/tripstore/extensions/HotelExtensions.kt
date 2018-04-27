package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.ItinHotel

fun ItinHotel.isPointOfSaleDifferentFromPointOfSupply(): Boolean {
    val posCurrencyCode = totalPriceDetails?.totalPOSCurrencyCode
    val posuCurrencyCode = totalPriceDetails?.primaryCurrencyCode
    if (posCurrencyCode != null && posuCurrencyCode != null) {
        return posCurrencyCode != posuCurrencyCode
    }
    return false
}
