package com.expedia.bookings.data.flights

import com.expedia.bookings.data.hotels.Hotel

class KrazyGlueHotel : Hotel() {
    val hotelName: String? = null
    val starRating: Float? = hotelStarRating
    val guestRating: Float? = hotelGuestRating
    val standAlonePrice: String? = null
    val airAttachedPrice: String? = null
    val hotelImage: String? = largeThumbnailUrl
}
