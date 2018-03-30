package com.expedia.bookings.hotel.data

import com.expedia.bookings.data.hotels.HotelReviewsResponse

data class TranslatedReview(val review: HotelReviewsResponse.Review, var showToUser: Boolean = true)
