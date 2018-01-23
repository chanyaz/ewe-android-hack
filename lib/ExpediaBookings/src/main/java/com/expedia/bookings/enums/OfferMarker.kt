package com.expedia.bookings.enums

enum class OfferMarker(private val stringValue: String) {
    LEADIN_PRICE("LEADIN_PRICE"),
    HIGHEST_STAR_RATING("HIGHEST_STAR_RATING"),
    HIGHEST_GUEST_RATING("HIGHEST_GUEST_RATING"),
    HIGHEST_DISCOUNT("HIGHEST_DISCOUNT");

    override fun toString(): String {
        return stringValue
    }
}
