package com.expedia.bookings.hotel.vm

import com.expedia.bookings.data.hotels.HotelReviewsSummaryResponse
import com.expedia.bookings.data.hotels.ReviewSummary
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.services.ReviewsServices
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class HotelReviewsSummaryViewModel(private val reviewsService: ReviewsServices) {

    val reviewSummarySubject = PublishSubject.create<ReviewSummary>()
    val noReviewSummarySubject = PublishSubject.create<Unit>()
    private var subscriptions: CompositeDisposable = CompositeDisposable()

    fun fetchReviewsSummary(hotelId: String) {
        subscriptions.clear()
        subscriptions.add(reviewsService.reviewsSummary(hotelId).subscribeObserver(reviewsSummaryObserver))
    }

    private val reviewsSummaryObserver = object : DisposableObserver<HotelReviewsSummaryResponse>() {

        override fun onNext(reviewsSummaryResponse: HotelReviewsSummaryResponse) {
            if (reviewsSummaryResponse.reviewSummaryCollection.reviewSummary.isNotEmpty()) {
                reviewSummarySubject.onNext(reviewsSummaryResponse.reviewSummaryCollection.reviewSummary[0])
            } else {
                noReviewSummarySubject.onNext(Unit)
            }
        }

        override fun onComplete() {
        }

        override fun onError(e: Throwable) {
            //TODO unhappy path
            noReviewSummarySubject.onNext(Unit)
        }
    }
}
