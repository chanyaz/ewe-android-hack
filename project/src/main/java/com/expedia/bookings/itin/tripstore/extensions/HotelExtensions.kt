package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.google.android.gms.maps.model.LatLng

fun ItinHotel.isPointOfSaleDifferentFromPointOfSupply(): Boolean {
    val posCurrencyCode = totalPriceDetails?.totalPOSCurrencyCode
    val posuCurrencyCode = totalPriceDetails?.primaryCurrencyCode
    if (posCurrencyCode != null && posuCurrencyCode != null) {
        return posCurrencyCode != posuCurrencyCode
    }
    return false
}

fun ItinHotel.buildSecondaryAddress(): String {
    val nullableArray = arrayOf(hotelPropertyInfo?.address?.city, hotelPropertyInfo?.address?.countrySubdivisionCode, hotelPropertyInfo?.address?.countryCode, hotelPropertyInfo?.address?.postalCode)
    val nonNullArray = nullableArray.filterNot { it.isNullOrBlank() }
    return nonNullArray.joinToString()
}

fun ItinHotel.getNameLocationPair(): Pair<String?, String?> {
    val name = this.hotelPropertyInfo?.name
    val location = buildSecondaryAddress()
    return Pair(name, location)
}

fun ItinHotel.getLatLng(): LatLng? {
    val lat = this.hotelPropertyInfo?.latitude
    val long = this.hotelPropertyInfo?.longitude
    return if (lat != null && long != null) {
        LatLng(lat, long)
    } else {
        null
    }
}
