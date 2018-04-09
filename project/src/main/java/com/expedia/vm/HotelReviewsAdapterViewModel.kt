package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelReviewsParams
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.data.hotels.HotelReviewsResponse.ReviewSummary
import com.expedia.bookings.data.hotels.ReviewSort
import com.expedia.bookings.hotel.data.TranslatedReview
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Constants
import com.expedia.util.endlessObserver
import io.reactivex.Observable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import java.util.Locale

class HotelReviewsAdapterViewModel(val hotelId: String, val reviewsServices: ReviewsServices, val locale: String) {
    val reviewsSummaryObservable = PublishSubject.create<ReviewSummary>()
    val translationUpdatedObservable = PublishSubject.create<String>()

    var translationMap: HashMap<String, TranslatedReview> = HashMap()

    val reviewsObservable = PublishSubject.create<Pair<ReviewSort, HotelReviewsResponse>>()

    val toggleReviewTranslationObserver = endlessObserver<String> { reviewId ->
        val alreadyTranslatedReview = translationMap[reviewId]
        if (alreadyTranslatedReview != null) {
            alreadyTranslatedReview.showToUser = !alreadyTranslatedReview.showToUser
            translationUpdatedObservable.onNext(reviewId)
        } else {
            getReviewFromApi(reviewId)
        }
    }

    val reviewsObserver = endlessObserver<ReviewSort> { reviewSort ->
        val params = HotelReviewsParams.Builder()
                .hotelId(hotelId)
                .pageNumber(reviewsPageNumber[reviewSort.value]++)
                .numReviewsPerPage(Constants.HOTEL_REVIEWS_PAGE_SIZE)
                .sortBy(reviewSort.sortByApiParam)
                .languageSort(locale)
                .build()

        reviewsDownloadsObservable.onNext(reviewsServices.reviews(params).map { Pair(reviewSort, it) })
    }

    private val reviewsPageNumber = IntArray(3)
    private val reviewsDownloadsObservable = PublishSubject.create<Observable<Pair<ReviewSort, HotelReviewsResponse>>>()
    private val orderedReviewsObservable = Observable.concat(reviewsDownloadsObservable)

    private val orderedReviewsObserver = object : DisposableObserver<Pair<ReviewSort, HotelReviewsResponse>>() {
        override fun onError(error: Throwable) {
            OmnitureTracking.trackReviewLoadingError(error.message ?: "")
        }

        override fun onNext(t: Pair<ReviewSort, HotelReviewsResponse>) {
            reviewsObservable.onNext(t)
        }

        override fun onComplete() {
        }
    }

    init {
        orderedReviewsObservable.subscribe(orderedReviewsObserver)

        reviewsObservable
                .map { it.second.reviewDetails.reviewSummaryCollection.reviewSummary[0] }
                .subscribe(reviewsSummaryObservable)
    }

    fun startDownloads() {
        reviewsObserver.onNext(ReviewSort.HIGHEST_RATING_FIRST)
        reviewsObserver.onNext(ReviewSort.LOWEST_RATING_FIRST)
        reviewsObserver.onNext(ReviewSort.NEWEST_REVIEW_FIRST)
    }

    private fun getReviewFromApi(reviewId: String) {
        reviewsServices.translate(reviewId, Locale.getDefault().language).subscribe ({ review ->
            val translatedReview = TranslatedReview(review)
            translationMap[reviewId] = translatedReview
            translationUpdatedObservable.onNext(reviewId)
        }, {
            translationUpdatedObservable.onNext(reviewId)
        })
    }
}
