package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelReviewsParams
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.data.hotels.HotelReviewsResponse.Review
import com.expedia.bookings.data.hotels.HotelReviewsResponse.ReviewSummary
import com.expedia.bookings.data.hotels.ReviewSort
import com.expedia.bookings.hotel.data.TranslatedReview
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.util.endlessObserver
import io.reactivex.Observable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import java.util.Locale

class HotelReviewsAdapterViewModel(val hotelId: String, val reviewsServices: ReviewsServices, val locale: String) {

    private val reviewsPageNumber = IntArray(3)

    val MIN_FAVORABLE_RATING = 3
    val PAGE_SIZE = 25

    val reviewsSummaryObservable = PublishSubject.create<ReviewSummary>()
    val favorableReviewsObservable = PublishSubject.create<List<Review>>()
    val criticalReviewsObservable = PublishSubject.create<List<Review>>()
    val newestReviewsObservable = PublishSubject.create<List<Review>>()
    val translationUpdatedObservable = PublishSubject.create<String>()

    var translationMap: HashMap<String, TranslatedReview> = HashMap()

    private val reviewsDownloadsObservable = PublishSubject.create<Observable<Pair<ReviewSort, HotelReviewsResponse>>>()
    private val orderedReviewsObservable = Observable.concat(reviewsDownloadsObservable)
    private val reviewsObservable = PublishSubject.create<Pair<ReviewSort, HotelReviewsResponse>>()

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
                .numReviewsPerPage(PAGE_SIZE)
                .sortBy(reviewSort.sortByApiParam)
                .languageSort(locale)
                .build()

        reviewsDownloadsObservable.onNext(reviewsServices.reviews(params).map { Pair(reviewSort, it) })
    }

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
