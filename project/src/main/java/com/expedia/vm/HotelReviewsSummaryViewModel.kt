package com.expedia.vm

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.TypefaceSpan
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

class HotelReviewsSummaryViewModel(val context: Context) {

    val overallRatingObservable = BehaviorSubject.create<String>()
    val roomCleanlinessObservable = BehaviorSubject.create<CharSequence>()
    val roomComfortObservable = BehaviorSubject.create<CharSequence>()
    val serviceStaffObservable = BehaviorSubject.create<CharSequence>()
    val hotelConditionObservable = BehaviorSubject.create<CharSequence>()


    val reviewsSummaryObserver = endlessObserver<HotelReviewsResponse.ReviewSummary> { reviewsSummary ->
        overallRatingObservable.onNext(getDisplayRating(reviewsSummary.avgOverallRating))
        roomCleanlinessObservable.onNext(getDisplayTextForRating(R.string.hotel_rating_room_cleanliness, reviewsSummary.cleanliness))
        roomComfortObservable.onNext(getDisplayTextForRating(R.string.hotel_rating_room_comfort, reviewsSummary.roomComfort))
        serviceStaffObservable.onNext(getDisplayTextForRating(R.string.hotel_rating_service_and_staff, reviewsSummary.serviceAndStaff))
        hotelConditionObservable.onNext(getDisplayTextForRating(R.string.hotel_rating_hotel_condition, reviewsSummary.hotelCondition))
    }

    private fun getDisplayTextForRating(displayStringId: Int, rating: Float): CharSequence {
        val displayRating = getDisplayRating(rating)
        val ratingString = Phrase.from(context, R.string.hotel_rating_summary_Template)
                .put("rating", displayRating)
                .put("attribute", context.resources.getString(displayStringId))
                .format()
                .toString()
        val builder = SpannableStringBuilder(ratingString)
        builder.setSpan(TypefaceSpan(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM)), 0, displayRating.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return builder
    }

    private fun getDisplayRating(rating: Float): String {
        return StrUtils.roundOff(rating, 1)
    }
}
