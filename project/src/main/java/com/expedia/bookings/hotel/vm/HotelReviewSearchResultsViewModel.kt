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

    @VisibleForTesting
    var currentQuery: String? = null
    @VisibleForTesting
    var currentHotelId: String? = null
    @VisibleForTesting
    var pageNumber = 0

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
        currentQuery = query
        currentHotelId = hotelId
        pageNumber = 0
        makeApiCall()
    }

    fun getNextPage() {
        pageNumber++
        makeApiCall()
    }

    @VisibleForTesting
    fun createSearchParams(): HotelReviewsParams? {
        if (!currentHotelId.isNullOrBlank() && !currentQuery.isNullOrBlank()) {
            return HotelReviewsParams.Builder()
                    .hotelId(currentHotelId)
                    .pageNumber(pageNumber)
                    .numReviewsPerPage(Constants.HOTEL_REVIEWS_PAGE_SIZE)
                    .sortBy("")
                    .searchTerm(currentQuery).build()
        }
        return null
    }

    private fun makeApiCall() {
        createSearchParams()?.let { params ->
            reviewsServices.reviewsSearch(params).subscribe(resultsObserver)
        }
    }
}
