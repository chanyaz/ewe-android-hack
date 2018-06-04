package com.expedia.bookings.data.hotels

enum class ReviewSort(val value: Int, val sortByApiParam: String) {
    NEWEST_REVIEW_FIRST(0, "DATEDESC"),
    HIGHEST_RATING_FIRST(1, "RATINGDESC"),
    LOWEST_RATING_FIRST(2, "RATINGASC");
}
