package com.expedia.vm

import com.expedia.bookings.R
import com.expedia.bookings.data.ReviewSort
import com.expedia.bookings.data.hotels.HotelReviewsParams
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.util.endlessObserver
import com.mobiata.android.BackgroundDownloader
import com.mobiata.android.Log
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.PublishSubject
import kotlin.properties.Delegates
import com.expedia.bookings.data.hotels.HotelReviewsResponse.Review

class HotelReviewsAdapterViewModel(val hotelId: String, val reviewsServices: ReviewsServices) {

    private val paramsBuilder = HotelReviewsParams.Builder()

    val reviewsObservable = PublishSubject.create<ReviewWrapper>()

    val reviewsObserver = endlessObserver<ReviewSort> { reviewSort ->
        // TO-DO: Loading only top 25 reviews for now.
        paramsBuilder.hotelId(hotelId)
                .pageNumber(0)
                .numReviewsPerPage(25)
                .sortBy(reviewSort.getSortByApiParam())
        reviewsServices.reviews(paramsBuilder.build(), generateReviewsServiceCallback(reviewSort))
    }

    private fun generateReviewsServiceCallback(reviewSort: ReviewSort): Observer<HotelReviewsResponse> {
        return object : Observer<HotelReviewsResponse> {
            override fun onNext(reviewsResponse: HotelReviewsResponse) {
                val reviewWrapper = ReviewWrapper()
                reviewWrapper.reviews = reviewsResponse.reviewDetails.reviewCollection.review
                reviewWrapper.reviewSort = reviewSort
                reviewsObservable.onNext(reviewWrapper)
            }

            override fun onCompleted() {
                // ignore
            }

            override fun onError(e: Throwable?) {
                Log.e("Hotel Reviews Error", e)
            }
        }
    }
}

class ReviewWrapper {
    var reviews: List<Review>? = null
    var reviewSort: ReviewSort? = null
}
