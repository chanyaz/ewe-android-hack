package com.expedia.vm

import android.content.Context
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.data.hotels.ReviewSort
import com.expedia.bookings.utils.Constants
import io.reactivex.subjects.PublishSubject

class HotelReviewsPageViewModel(val context: Context, private val reviewSort: ReviewSort, private val parentViewModel: HotelReviewsAdapterViewModel) {

    val loadMoreSubject = PublishSubject.create<Unit>()
    val reviewsAddedSubject = PublishSubject.create<HotelReviewsPageViewModel.ReviewUpdate>()

    private var hasReviews = false
    private val minFavorableRating = 3
    private var moreReviews = false

    init {
        subscribeToFilteredReviews()
        loadMoreSubject.map { reviewSort }.subscribe(parentViewModel.reviewsObserver)
    }

    private fun subscribeToFilteredReviews() {
        var observable = parentViewModel.reviewsObservable
                .filter { it.first == reviewSort }
                .map { it.second.reviewDetails.reviewCollection.review }

        if (reviewSort == ReviewSort.LOWEST_RATING_FIRST) {
            observable = observable.map { it.filter { it.ratingOverall < minFavorableRating } }
        } else if (reviewSort == ReviewSort.HIGHEST_RATING_FIRST) {
            observable = observable.map { it.filter { it.ratingOverall >= minFavorableRating } }
        }
        observable.subscribe(this::reviewsAdded)
    }

    private fun reviewsAdded(reviews: List<HotelReviewsResponse.Review>) {
        hasReviews = hasReviews || reviews.size > 0
        moreReviews = hasReviews && reviews.size >= Constants.HOTEL_REVIEWS_PAGE_SIZE && !ExpediaBookingApp.isAutomation()
        reviewsAddedSubject.onNext(ReviewUpdate(reviews, hasReviews, moreReviews))
    }

    data class ReviewUpdate(val newReviews: List<HotelReviewsResponse.Review>, val hasReviews: Boolean, val moreReviews: Boolean)
}
