package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject

public class HotelReviewsPageViewModel() {
    val reviewsScrollviewContainerObservable = BehaviorSubject.create<Boolean>()
    val messageProgressLoadingObservable = BehaviorSubject.create<Boolean>()
    val messageProgressLoadingAnimationObservable = BehaviorSubject.create<Unit>()

    val reviewObserver = endlessObserver<List<HotelReviewsResponse.Review>> { reviews ->
        reviewsScrollviewContainerObservable.onNext(reviews.size() > 0)
        messageProgressLoadingObservable.onNext(reviews.size() == 0)
        messageProgressLoadingAnimationObservable.onNext(Unit)
    }
}