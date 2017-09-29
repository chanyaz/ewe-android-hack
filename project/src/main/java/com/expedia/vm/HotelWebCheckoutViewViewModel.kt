package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.util.notNullAndObservable
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class HotelWebCheckoutViewViewModel(var context: Context): WebCheckoutViewViewModel(context) {

    var hotelSearchParamsObservable = BehaviorSubject.create<HotelSearchParams>()
    var offerObservable = BehaviorSubject.create<HotelOffersResponse.HotelRoomResponse>()
    val fireCreateTripObservable = PublishSubject.create<Unit>()

    var createTripViewModel by notNullAndObservable<HotelCreateTripViewModel> {
        it.tripResponseObservable.subscribe { createTripResponse ->
            webViewURLObservable.onNext("${PointOfSale.getPointOfSale().hotelsWebCheckoutURL}?tripid=${createTripResponse.tripId}")
        }
    }

    init {
        fireCreateTripObservable.subscribe { doCreateTrip() }
        offerObservable.map { Unit }.subscribe(fireCreateTripObservable)
    }

    override fun doCreateTrip() {
        showLoadingObservable.onNext(Unit)
        val numberOfAdults = hotelSearchParamsObservable.value.adults
        val childAges = hotelSearchParamsObservable.value.children
        val qualifyAirAttach = false
        createTripViewModel.tripParams.onNext(HotelCreateTripParams(offerObservable.value.productKey, qualifyAirAttach, numberOfAdults, childAges))
    }

}