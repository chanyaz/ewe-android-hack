package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject

public class HotelReviewsPageViewModel() {

    var hasReviews = false
    val reviewsScrollviewContainerObservable = BehaviorSubject.create<Boolean>()
    val messageProgressLoadingObservable = BehaviorSubject.create<Boolean>()
    val messageProgressLoadingAnimationObservable = BehaviorSubject.create<Unit>()

    val reviewsObserver = endlessObserver<List<HotelReviewsResponse.Review>> { reviews ->
        hasReviews = hasReviews || reviews.size() > 0
        reviewsScrollviewContainerObservable.onNext(hasReviews)
        messageProgressLoadingObservable.onNext(!hasReviews)
        messageProgressLoadingAnimationObservable.onNext(Unit)
    }
}