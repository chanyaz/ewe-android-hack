package com.expedia.vm

import android.content.Context
import android.text.TextUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelReviewsResponse.Review
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject

class HotelReviewRowViewModel(val context: Context) {

    val titleTextObservable = BehaviorSubject.create<String>()
    val reviewerTextObservable = BehaviorSubject.create<String>()
    val ratingObservable = BehaviorSubject.create<Float>()
    val submissionDateObservable = BehaviorSubject.create<String>()
    val reviewBodyObservable = BehaviorSubject.create<String>()

    val reviewObserver = endlessObserver<Review> { review ->
        titleTextObservable.onNext(review.title)
        reviewerTextObservable.onNext(getReviewerText(review))
        ratingObservable.onNext(review.ratingOverall.toFloat())
        submissionDateObservable.onNext(getSubmissionDate(review))
        reviewBodyObservable.onNext(review.reviewText)
    }

    private fun getSubmissionDate(review: Review): String {
        val dateTime = review.reviewSubmissionTime
        val submissionDateText = ProductFlavorFeatureConfiguration.getInstance().formatDateTimeForHotelUserReviews(context, dateTime)
        return submissionDateText
    }

    private fun getReviewerText(review: Review): String {
        var nameAndLocationText = ""
        val name = review.userDisplayName
        val location = review.userLocation
        val hasName = !TextUtils.isEmpty(name)
        val hasLocation = !TextUtils.isEmpty(location)

        if (hasName && hasLocation) {
            nameAndLocationText = context.resources.getString(R.string.user_review_name_and_location_signature, name, location)
        } else if (!hasName && hasLocation) {
            nameAndLocationText = location
        } else if (hasName && !hasLocation) {
            nameAndLocationText = name
        }
        return nameAndLocationText
    }
}