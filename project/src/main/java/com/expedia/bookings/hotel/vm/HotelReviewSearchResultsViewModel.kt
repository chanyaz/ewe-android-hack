package com.expedia.bookings.hotel.vm

import android.support.annotation.VisibleForTesting
import com.expedia.bookings.data.hotels.HotelReviewsParams
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.utils.Constants
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlin.properties.Delegates

class HotelReviewSearchResultsViewModel(compositeDisposable: CompositeDisposable) {

    var reviewsServices: ReviewsServices by Delegates.notNull()

    val reviewsObservable = PublishSubject.create<List<HotelReviewsResponse.Review>>()

    private val resultsObserver = object : Observer<HotelReviewsResponse> {
        override fun onError(e: Throwable) {
            // TODO
        }

        override fun onNext(response: HotelReviewsResponse) {
            reviewsObservable.onNext(response.reviewDetails.reviewCollection.review)
        }

        override fun onSubscribe(d: Disposable) {
            compositeDisposable.add(d)
        }

        override fun onComplete() {}
    }

    fun doSearch(query: String?, hotelId: String?) {
        createSearchParams(query, hotelId)?.let { params ->
            reviewsServices.reviewsSearch(params).subscribe(resultsObserver)
        }
    }

    @VisibleForTesting
    fun createSearchParams(query: String?, hotelId: String?): HotelReviewsParams? {
        if (!hotelId.isNullOrBlank() && !query.isNullOrBlank()) {
            return HotelReviewsParams.Builder()
                    .hotelId(hotelId)
                    .pageNumber(0)
                    .numReviewsPerPage(Constants.HOTEL_REVIEWS_PAGE_SIZE)
                    .sortBy("")
                    .languageSort("")
                    .searchTerm(query).build()
        }
        return null
    }
}
