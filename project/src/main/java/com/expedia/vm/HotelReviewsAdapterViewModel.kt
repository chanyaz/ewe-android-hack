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
import com.expedia.bookings.data.hotels.HotelReviewsResponse.ReviewSummary

class HotelReviewsAdapterViewModel(val hotelId: String, val reviewsServices: ReviewsServices) {

    private val paramsBuilder = HotelReviewsParams.Builder()

    val reviewsSummaryObservable = PublishSubject.create<ReviewSummary>()

    val favorableReviewsObservable = PublishSubject.create<List<Review>>()

    val criticalReviewsObservable = PublishSubject.create<List<Review>>()

    val newestReviewsObservable = PublishSubject.create<List<Review>>()

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
                val reviews = reviewsResponse.reviewDetails.reviewCollection.review
                val summary = reviewsResponse.reviewDetails.reviewSummaryCollection.reviewSummary.get(0)
                reviewsSummaryObservable.onNext(summary)
                // The reviews summary should be observed only once, since it does not change.
                reviewsSummaryObservable.onCompleted()
                when (reviewSort) {
                    ReviewSort.NEWEST_REVIEW_FIRST -> newestReviewsObservable.onNext(reviews)
                    ReviewSort.HIGHEST_RATING_FIRST -> favorableReviewsObservable.onNext(reviews)
                    ReviewSort.LOWEST_RATING_FIRST -> criticalReviewsObservable.onNext(reviews)
                }

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