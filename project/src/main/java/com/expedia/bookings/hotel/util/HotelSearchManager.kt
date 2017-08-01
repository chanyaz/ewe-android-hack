package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.subscribeObserver
import com.expedia.bookings.utils.RetrofitUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

open class HotelSearchManager(private val hotelServices: HotelServices?) {
    val successSubject = PublishSubject.create<HotelSearchResponse>()
    val errorSubject = PublishSubject.create<ApiError>()
    val noResultsSubject = PublishSubject.create<Unit>()
    val noInternetSubject = PublishSubject.create<Unit>()

    val apiCompleteSubject = PublishSubject.create<Unit>()

    var fetchingResults: Boolean = false
        private set

    private var searchResponse: HotelSearchResponse? = null
    private var subscriptions: CompositeDisposable = CompositeDisposable()

    private var prefetchSearch = false

    fun fetchResponse() : HotelSearchResponse? {
        return searchResponse
    }

    open fun doSearch(params: HotelSearchParams, prefetchSearch: Boolean = false) {
        hotelServices?.let { services ->
            this.prefetchSearch = prefetchSearch
            reset()
            fetchingResults = true
            subscriptions.add(services.search(params, apiCompleteSubject).subscribeObserver(searchResponseObserver))
        }
    }

    fun reset() {
        searchResponse = null
        subscriptions.clear()
    }

    fun dispose() {
        subscriptions.clear()
    }

    val searchResponseObserver = object : DisposableObserver<HotelSearchResponse>() {
        override fun onNext(hotelSearchResponse: HotelSearchResponse) {
            fetchingResults = false
            if (hotelSearchResponse.hasErrors()) {
                if (!prefetchSearch) {
                    errorSubject.onNext(hotelSearchResponse.firstError)
                }
            } else if (hotelSearchResponse.hotelList.isEmpty()) {
                if (!prefetchSearch) {
                    noResultsSubject.onNext(Unit)
                }
            } else {
                searchResponse = hotelSearchResponse
                successSubject.onNext(hotelSearchResponse)
            }
        }

        override fun onComplete() {
            fetchingResults = false
        }

        override fun onError(e: Throwable) {
            fetchingResults = false
            if (RetrofitUtils.isNetworkError(e) && !prefetchSearch) {
                noInternetSubject.onNext(Unit)
            }
        }
    }
}