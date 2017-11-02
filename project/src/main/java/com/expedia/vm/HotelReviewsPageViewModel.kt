package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.util.endlessObserver
import io.reactivex.subjects.BehaviorSubject

class HotelReviewsPageViewModel {

    var hasReviews = false
    val reviewsListObservable = BehaviorSubject.create<Boolean>()
    val messageProgressLoadingObservable = BehaviorSubject.create<Boolean>()
    val messageProgressLoadingAnimationObservable = BehaviorSubject.create<Unit>()
    val moreReviewsAvailableObservable = BehaviorSubject.createDefault<Boolean>(true)

    val reviewsObserver = endlessObserver<List<HotelReviewsResponse.Review>> { reviews ->
        hasReviews = hasReviews || reviews.size > 0
        reviewsListObservable.onNext(hasReviews)
        messageProgressLoadingObservable.onNext(!hasReviews)
        messageProgressLoadingAnimationObservable.onNext(Unit)
    }
}
