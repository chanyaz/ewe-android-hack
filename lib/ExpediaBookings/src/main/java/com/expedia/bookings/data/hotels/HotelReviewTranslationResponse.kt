package com.expedia.bookings.data.hotels

import com.google.gson.annotations.SerializedName

data class HotelReviewTranslationResponse(@SerializedName("Review") val review: HotelReviewsResponse.Review)
