package com.expedia.bookings.hotel.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.ReviewSummary
import com.expedia.bookings.utils.NumberUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject

class HotelReviewsSummaryBoxRatingViewModel(val context: Context) {

    val guestRatingObservable = BehaviorSubject.create<String>()
    val guestRatingRecommendationObservable = BehaviorSubject.create<String>()
    val barRatingViewVisibility = BehaviorSubject.create<Boolean>()
    val roomCleanlinessObservable = BehaviorSubject.create<ReviewSummaryDescriptionAndRating>()
    val roomComfortObservable = BehaviorSubject.create<ReviewSummaryDescriptionAndRating>()
    val serviceStaffObservable = BehaviorSubject.create<ReviewSummaryDescriptionAndRating>()
    val hotelConditionObservable = BehaviorSubject.create<ReviewSummaryDescriptionAndRating>()

    val reviewsSummaryObserver = endlessObserver<ReviewSummary> { reviewsSummary ->
        updateReviewsSummary(reviewsSummary)
    }

    val noReviewsSummaryObserver = endlessObserver<Unit> { barRatingViewVisibility.onNext(false) }

    private fun updateReviewsSummary(reviewsSummary: ReviewSummary) {
        updateBarRatingVisibility(reviewsSummary)
        guestRatingObservable.onNext(createGuestRatingString(reviewsSummary))
        guestRatingRecommendationObservable.onNext(createGuestRatingRecommendationString(reviewsSummary))
        roomCleanlinessObservable.onNext(ReviewSummaryDescriptionAndRating(context.getString(R.string.hotel_rating_room_cleanliness), getDisplayRating(reviewsSummary.cleanliness)))
        roomComfortObservable.onNext(
                ReviewSummaryDescriptionAndRating(
                        context.getString(R.string.hotel_rating_room_comfort), getDisplayRating(reviewsSummary.roomComfort)))
        serviceStaffObservable.onNext(
                ReviewSummaryDescriptionAndRating(
                        context.getString(R.string.hotel_rating_service_and_staff), getDisplayRating(reviewsSummary.serviceAndStaff)))
        hotelConditionObservable.onNext(
                ReviewSummaryDescriptionAndRating(
                        context.getString(R.string.hotel_rating_hotel_condition), getDisplayRating(reviewsSummary.hotelCondition)))
    }

    private fun updateBarRatingVisibility(reviewsSummary: ReviewSummary) {
        if (reviewsSummary.totalReviewCnt > 0 && hasAllRatingsForRatingsBar(reviewsSummary)) {
            barRatingViewVisibility.onNext(true)
        } else {
            barRatingViewVisibility.onNext(false)
        }
    }

    private fun hasAllRatingsForRatingsBar(reviewsSummary: ReviewSummary): Boolean {
        return reviewsSummary.cleanliness > 0f && reviewsSummary.serviceAndStaff > 0f &&
                reviewsSummary.hotelCondition > 0f && reviewsSummary.roomComfort > 0f
    }

    private fun createGuestRatingString(reviewsSummary: ReviewSummary): String {
        val displayRating = getDisplayRating(reviewsSummary.avgOverallRating).toString()
        return Phrase.from(context, R.string.hotel_guest_rating_out_of_five_TEMPLATE).put("rating", displayRating).format().toString()
    }

    private fun createGuestRatingRecommendationString(reviewsSummary: ReviewSummary): String {
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

    private fun getDisplayRating(rating: Float): Float {
        return NumberUtils.round(rating, 1)
    }

    data class ReviewSummaryDescriptionAndRating(val description: String, val rating: Float)
}
