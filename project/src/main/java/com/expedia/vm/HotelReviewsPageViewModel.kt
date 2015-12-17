package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject

public class HotelReviewsPageViewModel() {

    var hasReviews = false
    val reviewsListObservable = BehaviorSubject.create<Boolean>()
    val messageProgressLoadingObservable = BehaviorSubject.create<Boolean>()
    val messageProgressLoadingAnimationObservable = BehaviorSubject.create<Unit>()
    private val zeroReviewsReceivedSubject = BehaviorSubject.create<Boolean>(false)
    val moreReviewsAvailableObservable = zeroReviewsReceivedSubject.map{!it}.distinctUntilChanged()

    val reviewsObserver = endlessObserver<List<HotelReviewsResponse.Review>> { reviews ->
        hasReviews = hasReviews || reviews.size > 0
        zeroReviewsReceivedSubject.onNext(reviews.size == 0)
        reviewsListObservable.onNext(hasReviews)
        messageProgressLoadingObservable.onNext(!hasReviews)
        messageProgressLoadingAnimationObservable.onNext(Unit)
    }
}
