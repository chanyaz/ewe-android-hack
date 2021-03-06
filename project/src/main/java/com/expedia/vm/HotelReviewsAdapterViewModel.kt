package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelReviewsParams
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.data.hotels.HotelReviewsResponse.Review
import com.expedia.bookings.data.hotels.HotelReviewsResponse.ReviewSummary
import com.expedia.bookings.services.ReviewsServices
import com.expedia.util.endlessObserver
import rx.Observable
import rx.subjects.PublishSubject
import com.expedia.bookings.data.hotels.ReviewSort

class HotelReviewsAdapterViewModel(val hotelId: String, val reviewsServices: ReviewsServices, val locale: String) {
    
    private val reviewsPageNumber = IntArray(3)

    val MIN_FAVORABLE_RATING = 3

    val reviewsSummaryObservable = PublishSubject.create<ReviewSummary>()
    val favorableReviewsObservable = PublishSubject.create<List<Review>>()
    val criticalReviewsObservable = PublishSubject.create<List<Review>>()
    val newestReviewsObservable = PublishSubject.create<List<Review>>()

    private val reviewsDownloadsObservable = PublishSubject.create<Observable<Pair<ReviewSort, HotelReviewsResponse>>>()
    private val orderedReviewsObservable = Observable.concat(reviewsDownloadsObservable)
    private val reviewsObservable = PublishSubject.create<Pair<ReviewSort, HotelReviewsResponse>>()

    val reviewsObserver = endlessObserver<ReviewSort> { reviewSort ->
        val params = HotelReviewsParams.Builder()
                .hotelId(hotelId)
                .pageNumber(reviewsPageNumber[reviewSort.value]++)
                .numReviewsPerPage(25)
                .sortBy(reviewSort.sortByApiParam)
                .languageSort(locale)
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
                .map { it.filter { it.ratingOverall >= MIN_FAVORABLE_RATING } }
                .subscribe(favorableReviewsObservable)

        reviewsObservable
                .filter { it.first == ReviewSort.LOWEST_RATING_FIRST }
                .map { it.second.reviewDetails.reviewCollection.review }
                .map { it.filter { it.ratingOverall < MIN_FAVORABLE_RATING } }
                .subscribe(criticalReviewsObservable)
    }
}
