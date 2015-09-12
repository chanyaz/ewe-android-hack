package com.expedia.vm

import com.expedia.bookings.data.ReviewSort
import com.expedia.bookings.data.hotels.HotelReviewsParams
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.data.hotels.HotelReviewsResponse.Review
import com.expedia.bookings.data.hotels.HotelReviewsResponse.ReviewSummary
import com.expedia.bookings.services.ReviewsServices
import com.expedia.util.endlessObserver
import rx.Observable
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.PublishSubject

class HotelReviewsAdapterViewModel(val hotelId: String, val reviewsServices: ReviewsServices) {

    val reviewsSummaryObservable = PublishSubject.create<ReviewSummary>()
    val favorableReviewsObservable = PublishSubject.create<List<Review>>()
    val criticalReviewsObservable = PublishSubject.create<List<Review>>()
    val newestReviewsObservable = PublishSubject.create<List<Review>>()

    private val reviewsDownloadsObservable = PublishSubject.create<Observable<Pair<ReviewSort, HotelReviewsResponse>>>()
    private val orderedReviewsObservable = Observable.concat(reviewsDownloadsObservable)
    private val reviewsObservable = PublishSubject.create<Pair<ReviewSort, HotelReviewsResponse>>()

    val reviewsObserver = endlessObserver<ReviewSort> { reviewSort ->
        // TODO: Loading only top 25 reviews for now.
        val params = HotelReviewsParams.Builder()
                .hotelId(hotelId)
                .pageNumber(0)
                .numReviewsPerPage(25)
                .sortBy(reviewSort.getSortByApiParam())
                .build()

        reviewsDownloadsObservable.onNext(reviewsServices.reviews(params).map { Pair(reviewSort, it) })
    }

    init {
        orderedReviewsObservable.subscribe {
            reviewsObservable.onNext(it)
        }

        reviewsObservable
                .map { it.second.reviewDetails.reviewSummaryCollection.reviewSummary.get(0) }
                .subscribe(reviewsSummaryObservable)

        reviewsObservable
                .filter { it.first == ReviewSort.NEWEST_REVIEW_FIRST }
                .map { it.second.reviewDetails.reviewCollection.review }
                .subscribe(newestReviewsObservable)

        reviewsObservable
                .filter { it.first == ReviewSort.HIGHEST_RATING_FIRST }
                .map { it.second.reviewDetails.reviewCollection.review }
                .subscribe(favorableReviewsObservable)

        reviewsObservable
                .filter { it.first == ReviewSort.LOWEST_RATING_FIRST }
                .map { it.second.reviewDetails.reviewCollection.review }
                .subscribe(criticalReviewsObservable)
    }
}