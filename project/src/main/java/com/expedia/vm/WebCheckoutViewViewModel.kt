package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.util.notNullAndObservable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class WebCheckoutViewViewModel : WebViewViewModel() {
    val bookedTripIDObservable = BehaviorSubject.create<String>()
    val closeView = PublishSubject.create<Unit>()
    var offerObservable = BehaviorSubject.create<HotelOffersResponse.HotelRoomResponse>()
    var hotelSearchParamsObservable = BehaviorSubject.create<HotelSearchParams>()
    val fireCreateTripObservable = PublishSubject.create<Unit>()
    var createTripViewModel by notNullAndObservable<HotelCreateTripViewModel> {
        it.tripResponseObservable.subscribe { createTripResponse ->
            webViewURLObservable.onNext("${PointOfSale.getPointOfSale().hotelsWebCheckoutURL}?tripid=${createTripResponse.tripId}")
        }
    }

    init {
        offerObservable.map { Unit }.subscribe(fireCreateTripObservable)
        fireCreateTripObservable.subscribe { doCreateTrip() }
    }

    fun doCreateTrip() {
        val numberOfAdults = hotelSearchParamsObservable.value.adults
        val childAges = hotelSearchParamsObservable.value.children
        val qualifyAirAttach = false
        createTripViewModel.tripParams.onNext(HotelCreateTripParams(offerObservable.value.productKey, qualifyAirAttach, numberOfAdults, childAges))
    }
}
