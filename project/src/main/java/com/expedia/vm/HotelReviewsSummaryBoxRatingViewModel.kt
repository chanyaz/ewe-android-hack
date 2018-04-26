package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.ReviewSummary
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.NumberUtils
import com.squareup.phrase.Phrase

class HotelReviewsSummaryBoxRatingViewModel(val context: Context,
                                            private val reviewsSummary: ReviewSummary,
                                            private val numberOfReviews: Int? = null) {

    val guestRatingString: String by lazy { createGuestRatingString() }
    val guestRatingRecommendationString: String by lazy { createGuestRatingRecommendationString() }
    val numberOfReviewsString: String? by lazy { createNumberOfReviewsString() }

    val roomCleanlinessReviewSummary: ReviewSummaryDescriptionAndRating by lazy {
        ReviewSummaryDescriptionAndRating(context.getString(R.string.hotel_rating_room_cleanliness), getDisplayRating(reviewsSummary.cleanliness))
    }
    val roomComfortReviewSummary: ReviewSummaryDescriptionAndRating by lazy {
        ReviewSummaryDescriptionAndRating(context.getString(R.string.hotel_rating_room_comfort), getDisplayRating(reviewsSummary.roomComfort))
    }
    val serviceStaffReviewSummary: ReviewSummaryDescriptionAndRating by lazy {
        ReviewSummaryDescriptionAndRating(context.getString(R.string.hotel_rating_service_and_staff), getDisplayRating(reviewsSummary.serviceAndStaff))
    }
    val hotelConditionReviewSummary: ReviewSummaryDescriptionAndRating by lazy {
        ReviewSummaryDescriptionAndRating(context.getString(R.string.hotel_rating_hotel_condition), getDisplayRating(reviewsSummary.hotelCondition))
    }

    private fun createGuestRatingString(): String {
        val displayRating = getDisplayRating(reviewsSummary.avgOverallRating).toString()
        return Phrase.from(context, R.string.hotel_guest_rating_out_of_five_TEMPLATE).put("rating", displayRating).format().toString()
    }

    private fun createGuestRatingRecommendationString(): String {
        val roundedRating = getDisplayRating(reviewsSummary.avgOverallRating)
        return when {
            (roundedRating < 3.5f) -> context.getString(R.string.hotel_guest_rating)
            (roundedRating < 4f) -> context.getString(R.string.hotel_guest_recommend_good)
            (roundedRating < 4.3f) -> context.getString(R.string.hotel_guest_recommend_very_good)
            (roundedRating < 4.5f) -> context.getString(R.string.hotel_guest_recommend_excellent)
            (roundedRating < 4.7f) -> context.getString(R.string.hotel_guest_recommend_wonderful)
            else -> context.getString(R.string.hotel_guest_recommend_exceptional)
        }
    }

    private fun createNumberOfReviewsString(): String? {
        return if (numberOfReviews != null)
            context.resources.getQuantityString(R.plurals.hotel_number_of_reviews, numberOfReviews, HotelUtils.formattedReviewCount(numberOfReviews))
        else null
    }

    private fun getDisplayRating(rating: Float): Float {
        return NumberUtils.round(rating, 1)
    }

    data class ReviewSummaryDescriptionAndRating(val description: String, val rating: Float)
}
