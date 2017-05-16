package com.expedia.bookings.hotel.provider

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.RetrofitUtils
import rx.Observer
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription

open class HotelSearchProvider(private val hotelServices: HotelServices?) {
    val successSubject = PublishSubject.create<HotelSearchResponse>()
    val errorSubject = PublishSubject.create<ApiError>()
    val noResultsSubject = PublishSubject.create<Unit>()
    val noInternetSubject = PublishSubject.create<Unit>()

    val apiCompleteSubject = PublishSubject.create<Unit>()

    private var subscriptions: CompositeSubscription = CompositeSubscription()

    fun doSearch(params: HotelSearchParams) {
        hotelServices?.let { services ->
            subscriptions.add(services.search(params, apiCompleteSubject,
                    hitLPAS = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelLPASEndpoint))
                    .subscribe(searchResponseObserver))
        }
    }

    fun unsubscribe() {
        subscriptions.clear()
    }

    val searchResponseObserver = object : Observer<HotelSearchResponse> {
        override fun onNext(hotelSearchResponse: HotelSearchResponse) {
            if (hotelSearchResponse.hasErrors()) {
                errorSubject.onNext(hotelSearchResponse.firstError)
            } else if (hotelSearchResponse.hotelList.isEmpty()) {
                noResultsSubject.onNext(Unit)
            } else {
                successSubject.onNext(hotelSearchResponse)
            }
        }

        override fun onCompleted() {
            // do nothing
        }

        override fun onError(e: Throwable?) {
            if (RetrofitUtils.isNetworkError(e)) {
                noInternetSubject.onNext(Unit)
            }
        }
    }
}